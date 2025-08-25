package cn.longwingstech.intelligence.longaicodemother.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.longwingstech.intelligence.longaicodemother.ai.PromptAuthServiceFactory;
import cn.longwingstech.intelligence.longaicodemother.ai.memory.RedisChatMemoryStore;
import cn.longwingstech.intelligence.longaicodemother.ai.model.PromptAuthResult;
import cn.longwingstech.intelligence.longaicodemother.constant.AppConstant;
import cn.longwingstech.intelligence.longaicodemother.core.AiCodeGeneratorFacade;
import cn.longwingstech.intelligence.longaicodemother.core.builder.VueProjectBuilder;
import cn.longwingstech.intelligence.longaicodemother.core.handler.StreamHandlerExecutor;
import cn.longwingstech.intelligence.longaicodemother.exception.BusinessException;
import cn.longwingstech.intelligence.longaicodemother.exception.ErrorCode;
import cn.longwingstech.intelligence.longaicodemother.exception.ThrowUtils;
import cn.longwingstech.intelligence.longaicodemother.mapper.AppMapper;
import cn.longwingstech.intelligence.longaicodemother.model.dto.app.AppQueryRequest;
import cn.longwingstech.intelligence.longaicodemother.model.entity.App;
import cn.longwingstech.intelligence.longaicodemother.model.entity.User;
import cn.longwingstech.intelligence.longaicodemother.model.enums.ChatHistoryMessageTypeEnum;
import cn.longwingstech.intelligence.longaicodemother.model.enums.CodeGenTypeEnum;
import cn.longwingstech.intelligence.longaicodemother.model.vo.AppVO;
import cn.longwingstech.intelligence.longaicodemother.model.vo.UserVO;
import cn.longwingstech.intelligence.longaicodemother.monitor.MonitorContext;
import cn.longwingstech.intelligence.longaicodemother.monitor.MonitorContextHolder;
import cn.longwingstech.intelligence.longaicodemother.service.AppService;
import cn.longwingstech.intelligence.longaicodemother.service.UserService;
import cn.longwingstech.intelligence.longaicodemother.service.ChatHistoryService;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 *
 */
@Service
@Slf4j
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private UserService userService;

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private StreamHandlerExecutor streamHandlerExecutor;

    @Resource
    private VueProjectBuilder vueProjectBuilder;
    @Resource
    private PromptAuthServiceFactory promptAuthServiceFactory;

    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;

    @Value("${deploy.code_deploy_host}")
    private String code_deploy_host;

    @Override
    public Flux<String> chatToGenCode(Long appId, String message,User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 权限校验，仅本人可以和自己的应用对话
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenType = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用代码生成类型错误");
        }
        //5. 验证提示词
        PromptAuthResult promptAuthResult = promptAuthServiceFactory.create(appId.toString()).promptAuth(message);
        if (!promptAuthResult.getResult()) {
            return Flux.just(promptAuthResult.getMessage());
        }
        // 6. 在调用 AI 前，先保存用户消息到数据库中
//        chatHistoryService.addChatMessage(appId, message, ChatHistoryMessageTypeEnum.USER.getValue(), loginUser.getId());
        // 7. 设置上下文
        MonitorContextHolder.setContext(
                MonitorContext.builder()
                        .appId(appId.toString())
                        .userId(loginUser.getId().toString())
                        .build()
        );
        // 8. 历史记录压缩。避免历史记录过多
//        MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder()
//                .id(appId)
//                .maxMessages(20)
//                .chatMemoryStore(redisChatMemoryStore)
//                .build();

        // 9. 调用 AI 生成代码
        Flux<String> flux = aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
        // 10. 流式处理
        return streamHandlerExecutor.doExecute(flux,chatHistoryService,appId,loginUser,codeGenTypeEnum)
                .doFinally(signalType -> {
                    // 流结束时清理（无论成功/失败/取消）
                    MonitorContextHolder.clearContext();
                });
    }

    @Override
    public String deployApp(Long appId,User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 错误");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 权限校验，仅本人可以部署自己的应用
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 如果没有，则生成 6 位 deployKey（字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，获取原始代码生成路径（应用访问目录）
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. Vue 项目特殊处理：执行构建
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenType);
        if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
            // Vue 项目需要构建
            boolean buildSuccess = vueProjectBuilder.buildProject(sourceDirPath);
            ThrowUtils.throwIf(!buildSuccess, ErrorCode.SYSTEM_ERROR, "Vue 项目构建失败，请检查代码和依赖");
            // 检查 dist 目录是否存在
            File distDir = new File(sourceDirPath, "dist");
            ThrowUtils.throwIf(!distDir.exists(), ErrorCode.SYSTEM_ERROR, "Vue 项目构建完成但未生成 dist 目录");
            // 将 dist 目录作为部署源
            sourceDir = distDir;
            log.info("Vue 项目构建成功，将部署 dist 目录: {}", distDir.getAbsolutePath());
        }
        // 8. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;

        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用部署失败：" + e.getMessage());
        }
        // 9. 更新数据库
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 10. 返回可访问的 URL 地址
        return String.format("%s/%s", code_deploy_host, deployKey);
    }

    @Override
    public AppVO getAppVO(App app) {
        if (app == null) {
            return null;
        }
        AppVO appVO = new AppVO();
        BeanUtil.copyProperties(app, appVO);
        // 关联查询用户信息
        Long userId = app.getUserId();
        if (userId != null) {
            User user = userService.getById(userId);
            UserVO userVO = BeanUtil.toBean(user, UserVO.class);
            appVO.setUser(userVO);
        }
        return appVO;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        // 批量获取用户信息，避免 N+1 查询问题
        Set<Long> userIds = appList.stream()
                .map(App::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserVO> userVOMap = userService.listByIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, userService::getUserVO));
        return appList.stream().map(app -> {
            AppVO appVO = getAppVO(app);
            UserVO userVO = userVOMap.get(app.getUserId());
            appVO.setUser(userVO);
            return appVO;
        }).collect(Collectors.toList());
    }

    @Override
    public QueryWrapper getQueryWrapper(AppQueryRequest appQueryRequest) {
        if (appQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = appQueryRequest.getId();
        String appName = appQueryRequest.getAppName();
        String cover = appQueryRequest.getCover();
        String initPrompt = appQueryRequest.getInitPrompt();
        String codeGenType = appQueryRequest.getCodeGenType();
        if (codeGenType!=null && codeGenType.isEmpty()) {
            codeGenType = null;
        }
        String deployKey = appQueryRequest.getDeployKey();
        Integer priority = appQueryRequest.getPriority();
        Long userId = appQueryRequest.getUserId();
        String sortField = appQueryRequest.getSortField();
        String sortOrder = appQueryRequest.getSortOrder();
        return QueryWrapper.create()
                .eq("id", id)
                .like("appName", appName)
                .like("cover", cover)
                .like("initPrompt", initPrompt)
                .eq("codeGenType", codeGenType)
                .eq("deployKey", deployKey)
                .eq("priority", priority)
                .eq("userId", userId)
                .orderBy(sortField, "ascend".equals(sortOrder));
    }

    @Override
    public Boolean deleteAppWithHistory(Long appId) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
        
        // 验证应用是否存在
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        
        try {
            // 先删除对话历史
            chatHistoryService.deleteByAppId(appId);
            // 再删除应用
            boolean result = this.removeById(appId);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "删除应用失败");
            
            return true;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "删除应用失败：" + e.getMessage());
        }
    }
}
