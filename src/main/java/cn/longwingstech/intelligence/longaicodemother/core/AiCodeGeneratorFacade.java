package cn.longwingstech.intelligence.longaicodemother.core;

import cn.hutool.json.JSONUtil;
import cn.longwingstech.intelligence.longaicodemother.ai.AiCodeGeneratorService;
import cn.longwingstech.intelligence.longaicodemother.ai.AiCodeGeneratorServiceFactory;
import cn.longwingstech.intelligence.longaicodemother.ai.model.HtmlCodeResult;
import cn.longwingstech.intelligence.longaicodemother.ai.model.MultiFileCodeResult;
import cn.longwingstech.intelligence.longaicodemother.ai.model.message.AiResponseMessage;
import cn.longwingstech.intelligence.longaicodemother.ai.model.message.ToolExecutedMessage;
import cn.longwingstech.intelligence.longaicodemother.ai.model.message.ToolRequestMessage;
import cn.longwingstech.intelligence.longaicodemother.constant.AppConstant;
import cn.longwingstech.intelligence.longaicodemother.core.builder.VueProjectBuilder;
import cn.longwingstech.intelligence.longaicodemother.core.saver.CodeFileSaverExecutor;
import cn.longwingstech.intelligence.longaicodemother.exception.BusinessException;
import cn.longwingstech.intelligence.longaicodemother.exception.ErrorCode;
import cn.longwingstech.intelligence.longaicodemother.model.enums.CodeGenTypeEnum;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.TokenStream;
import dev.langchain4j.service.tool.ToolExecution;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;

/**
 * AI 代码生成外观类，组合生成和保存功能
 */
@Slf4j
@Service
public class AiCodeGeneratorFacade {

    @Resource
    private AiCodeGeneratorServiceFactory aiCodeGeneratorServiceFactory;

    @Resource
    private VueProjectBuilder vueProjectBuilder;

    /**
     * 统一入口：根据类型生成并保存代码（流式）
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     */
    public Flux<String> generateAndSaveCodeStream(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appid) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        AiCodeGeneratorService generatorService = aiCodeGeneratorServiceFactory
                .getAiCodeGeneratorService(appid.toString(), codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                Flux<String> codeStream = generatorService.generateHtmlCodeStream(appid.toString(),userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.HTML, appid);
            }
            case MULTI_FILE -> {
                Flux<String> codeStream = generatorService.generateMultiFileCodeStream(appid.toString(),userMessage);
                yield processCodeStream(codeStream, CodeGenTypeEnum.MULTI_FILE, appid);
            }
            case  VUE_PROJECT -> {
                TokenStream tokenStream = generatorService.generateVueProjectCodeStream(appid.toString(), userMessage);
                yield processTokenStream(tokenStream,appid);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }

    /**
     * 统一入口：根据类型生成并保存代码
     *
     * @param userMessage     用户提示词
     * @param codeGenTypeEnum 生成类型
     * @return 保存的目录
     * @deprecated 弃用，请使用 generateAndSaveCodeStream 方法
     */
    @Deprecated
    public File generateAndSaveCode(String userMessage, CodeGenTypeEnum codeGenTypeEnum, Long appid) {
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成类型为空");
        }
        AiCodeGeneratorService aiCodeGeneratorService = aiCodeGeneratorServiceFactory.getAiCodeGeneratorService(appid.toString(), codeGenTypeEnum);
        return switch (codeGenTypeEnum) {
            case HTML -> {
                HtmlCodeResult result =aiCodeGeneratorService.generateHtmlCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.HTML, appid);
            }
            case MULTI_FILE -> {
                MultiFileCodeResult result = aiCodeGeneratorService.generateMultiFileCode(userMessage);
                yield CodeFileSaverExecutor.executeSaver(result, CodeGenTypeEnum.MULTI_FILE,appid);
            }
            default -> {
                String errorMessage = "不支持的生成类型：" + codeGenTypeEnum.getValue();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, errorMessage);
            }
        };
    }


    /**
     * tokenStream 转 flux方法 又称 适配器
     * @param tokenStream
     * @return
     */
    private Flux<String> processTokenStream(TokenStream tokenStream,Long appId) {
        return Flux.create(sink -> {
                    // 普通AI消息处理
            tokenStream.onPartialResponse((String partialResponse) -> {
                        AiResponseMessage aiResponseMessage = new AiResponseMessage(partialResponse);
                        sink.next(JSONUtil.toJsonStr(aiResponseMessage));
                    })
                    // 工具执行请求处理(工具执行前回调)
                    .onPartialToolExecutionRequest((index, toolExecutionRequest) -> {
                        try {
                            ToolRequestMessage toolRequestMessage = new ToolRequestMessage(toolExecutionRequest);
                            sink.next(JSONUtil.toJsonStr(toolRequestMessage));
                        } catch (Exception e) {
                            log.error(e.getMessage());
                            log.error("工具执行请求处理(工具执行前回调)");
                        }
                    })
                    // 工具执行请求处理(工具执行完成后回调)
                    .onToolExecuted((ToolExecution toolExecution) -> {
                        try {
                            ToolExecutedMessage toolExecutedMessage = new ToolExecutedMessage(toolExecution);
                            sink.next(JSONUtil.toJsonStr(toolExecutedMessage));
                        } catch (Exception e) {
                            log.error(e.getMessage());
                            log.error("工具执行请求处理(工具执行完成后回调)");
                        }
                    })
                    // 完整响应处理
                    .onCompleteResponse((ChatResponse response) -> {
                        // 异步构造 Vue 项目
                        String projectPath = AppConstant.CODE_OUTPUT_ROOT_DIR + "/vue_project_" + appId;
                        vueProjectBuilder.buildProject(projectPath);
                        sink.complete();
                    })
                    // 错误处理
                    .onError((Throwable error) -> {
                        log.error(error.getMessage());
                        sink.error(error);
                    })
                    // 开始处理
                    .start();

        });
    }

    /**
     * 通用流式代码处理方法
     *
     * @param codeStream  代码流
     * @param codeGenType 代码生成类型
     * @return 流式响应
     */
    private Flux<String> processCodeStream(Flux<String> codeStream, CodeGenTypeEnum codeGenType,Long appid) {
        StringBuilder codeBuilder = new StringBuilder();
        return codeStream.doOnNext(chunk -> {
            // 实时收集代码片段
            codeBuilder.append(chunk);
        }).doOnComplete(() -> {
            // 流式返回完成后保存代码
            try {
                String completeCode = codeBuilder.toString();
                // 使用执行器解析代码
                Object parsedResult = CodeParserExecutor.executeParser(completeCode, codeGenType);
                // 使用执行器保存代码
                File savedDir = CodeFileSaverExecutor.executeSaver(parsedResult, codeGenType,appid);
                log.info("保存成功，路径为：" + savedDir.getAbsolutePath());
            } catch (Exception e) {
                log.error("保存失败: {}", e.getMessage());
            }
        });
    }
}
