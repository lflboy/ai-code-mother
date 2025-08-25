package cn.longwingstech.intelligence.longaicodemother.ai;

import cn.longwingstech.intelligence.longaicodemother.ai.guardrail.PromptSafetyInputGuardrail;
import cn.longwingstech.intelligence.longaicodemother.ai.guardrail.RetryOutputGuardrail;
import cn.longwingstech.intelligence.longaicodemother.ai.memory.RedisChatMemoryStore;
import cn.longwingstech.intelligence.longaicodemother.ai.tools.FileWriteTool;
import cn.longwingstech.intelligence.longaicodemother.ai.tools.ToolManager;
import cn.longwingstech.intelligence.longaicodemother.model.enums.CodeGenTypeEnum;
import cn.longwingstech.intelligence.longaicodemother.service.ChatMessageService;
import cn.longwingstech.intelligence.longaicodemother.utils.SpringContextUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@Slf4j
public class AiCodeGeneratorServiceFactory {
    @Resource
    private RedisChatMemoryStore redisChatMemoryStore;
    @Resource
    private ToolManager toolManager;
    @Resource
    private ChatMessageService chatMessageService;
    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, AiCodeGeneratorService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，缓存键: {}, 原因: {}", key, cause);
            })
            .build();

    /**
     * 通过appid和codeGenType创建AiCodeGeneratorService
     *
     * @param appid
     * @param codeGenType
     * @return
     */
    public AiCodeGeneratorService getAiCodeGeneratorService(String appid, CodeGenTypeEnum codeGenType) {
        String codeKey = buildCodeKey(appid, codeGenType);
        StreamingChatModel prototypeStreamingChatModel =
                SpringContextUtil.getBean("prototypeStreamingChatModel", StreamingChatModel.class);

        ChatModel prototypeChatModel = SpringContextUtil.getBean("prototypeChatModel", ChatModel.class);
        // 先查询缓存，缓存不存在才创建
        return serviceCache.get(codeKey, key -> {
            // 创建消息窗口缓存 每个客户端创建一个
            MessageWindowChatMemory messageWindowChatMemory = MessageWindowChatMemory.builder()
                    .id(appid)
                    .maxMessages(50)
                    .chatMemoryStore(redisChatMemoryStore)
                    .build();
            // 加载历史消息
            {
                messageWindowChatMemory.clear();
                List<String> list = chatMessageService.loadHistory(Long.parseLong(appid), 50);
                if (!list.isEmpty()) {
                    List<ChatMessage> messages = list.stream().map(ChatMessageDeserializer::messageFromJson).toList().reversed();
                    messageWindowChatMemory.add(messages);
                }
            }
            return switch (codeGenType) {
                case VUE_PROJECT -> AiServices.builder(AiCodeGeneratorService.class)
                        .streamingChatModel(prototypeStreamingChatModel)
                        .chatModel(prototypeChatModel)
                        .inputGuardrails(new PromptSafetyInputGuardrail()) //输入护轨
                        .tools(toolManager.getAllTools())
                        .chatMemoryProvider(memoryId -> messageWindowChatMemory)
                        .maxSequentialToolsInvocations(30) // 允许最多20个工具调用
                        // 处理工具调用幻觉问题
                        .hallucinatedToolNameStrategy(toolExecutionRequest ->
                                ToolExecutionResultMessage.from(toolExecutionRequest,
                                        "Error: there is no tool called " + toolExecutionRequest.name())
                        )
                        .build();
                case HTML, MULTI_FILE -> AiServices.builder(AiCodeGeneratorService.class)
                        .streamingChatModel(prototypeStreamingChatModel)
                        .chatModel(prototypeChatModel)
                        .inputGuardrails(new PromptSafetyInputGuardrail()) //输入护轨
                        .chatMemoryProvider(memoryId -> messageWindowChatMemory)
                        .hallucinatedToolNameStrategy(toolExecutionRequest ->
                                ToolExecutionResultMessage.from(toolExecutionRequest,
                                        "Error: there is no tool called " + toolExecutionRequest.name())
                        )
                        .build();
                default -> throw new IllegalArgumentException("不支持的代码生成类型");
            };
        });
    }

    /**
     * 获取标题生成ai实例
     *
     * @return
     */
    public TitleGeneratorService titleGeneratorService() {

        ChatModel prototypeChatModel = SpringContextUtil.getBean("prototypeChatModel", ChatModel.class);

        return AiServices.builder(TitleGeneratorService.class)
                .chatModel(prototypeChatModel)
                .build();
    }

    /**
     * @param appid
     * @param codeGenType
     * @return
     */
    private String buildCodeKey(String appid, CodeGenTypeEnum codeGenType) {
        return appid + "_" + codeGenType.getValue();
    }

}
