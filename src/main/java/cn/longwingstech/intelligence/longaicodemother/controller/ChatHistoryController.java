package cn.longwingstech.intelligence.longaicodemother.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.longwingstech.intelligence.longaicodemother.common.BaseResponse;
import cn.longwingstech.intelligence.longaicodemother.common.DeleteRequest;
import cn.longwingstech.intelligence.longaicodemother.common.ResultUtils;
import cn.longwingstech.intelligence.longaicodemother.constant.UserConstant;
import cn.longwingstech.intelligence.longaicodemother.exception.ErrorCode;
import cn.longwingstech.intelligence.longaicodemother.exception.ThrowUtils;
import cn.longwingstech.intelligence.longaicodemother.model.dto.chathistory.ChatHistoryQueryRequest;
import cn.longwingstech.intelligence.longaicodemother.model.entity.ChatHistory;
import cn.longwingstech.intelligence.longaicodemother.model.entity.User;
import cn.longwingstech.intelligence.longaicodemother.service.ChatHistoryService;
import cn.longwingstech.intelligence.longaicodemother.service.UserService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 对话历史 控制层。
 */
@RestController
@RequestMapping("/chatHistory")
@SaCheckLogin
@Tag(name = "对话历史接口")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 分页查询某个应用的对话历史（游标查询）
     *
     * @param appId          应用ID
     * @param pageSize       页面大小
     * @param lastCreateTime 最后一条记录的创建时间
     * @return 对话历史分页
     */
    @GetMapping("/app/{appId}")
    @Operation(summary = "分页查询某个应用的对话历史")
    public BaseResponse<Page<ChatHistory>> listAppChatHistory(@PathVariable Long appId,
                                                              @RequestParam(defaultValue = "10") int pageSize,
                                                              @RequestParam(required = false) LocalDateTime lastCreateTime) {
        User loginUser = userService.getLoginUser();
        Page<ChatHistory> result = chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 管理员分页查询所有对话历史
     *
     * @param chatHistoryQueryRequest 查询请求
     * @return 对话历史分页
     */
    @PostMapping("/admin/list/page/vo")
    @SaCheckRole(value = UserConstant.ADMIN_ROLE)
    @Operation(summary = "管理员分页查询所有对话历史")
    public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryRequest chatHistoryQueryRequest) {
        ThrowUtils.throwIf(chatHistoryQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = chatHistoryQueryRequest.getPageNum();
        long pageSize = chatHistoryQueryRequest.getPageSize();
        // 查询数据
        QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryRequest);
        Page<ChatHistory> result = chatHistoryService.page(Page.of(pageNum, pageSize), queryWrapper);
        return ResultUtils.success(result);
    }


}
