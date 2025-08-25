package cn.longwingstech.intelligence.longaicodemother.ai.memory;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.longwingstech.intelligence.longaicodemother.ai.AiCodeGeneratorService;
import cn.longwingstech.intelligence.longaicodemother.ai.model.message.ToolExecutedMessage;
import cn.longwingstech.intelligence.longaicodemother.ai.tools.BaseTool;
import cn.longwingstech.intelligence.longaicodemother.ai.tools.ToolManager;
import cn.longwingstech.intelligence.longaicodemother.mapper.AppMapper;
import cn.longwingstech.intelligence.longaicodemother.model.entity.App;
import cn.longwingstech.intelligence.longaicodemother.model.entity.ChatHistory;
import cn.longwingstech.intelligence.longaicodemother.model.enums.CodeGenTypeEnum;
import cn.longwingstech.intelligence.longaicodemother.model.enums.MessageTypeEnum;
import cn.longwingstech.intelligence.longaicodemother.service.AppService;
import cn.longwingstech.intelligence.longaicodemother.service.ChatHistoryService;
import cn.longwingstech.intelligence.longaicodemother.service.ChatMessageService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.Lists;
import com.mybatisflex.core.query.QueryWrapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.*;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 将消息实时保存到redis
 * 异步保存到MYSQL
 */
@Component
@Slf4j
public class RedisChatMemoryStore implements ChatMemoryStore {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private String REDIS_KEY_PREFIX = "chat:";

    @Resource
    private ChatHistoryService chatHistoryService;
    @Resource
    private ToolManager toolManager;
    @Resource
    private AppMapper appMapper;
    private static final Cache<Long, Long> userIdCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("实例被移除，缓存键: {}, 原因: {}", key, cause);
            })
            .build();
    @Resource
    private ChatMessageService chatMessageService;

    @Override
    public List<ChatMessage> getMessages(Object o) {
        // TODO: 实现通过内存ID从持久化存储中获取所有消息。
        // 可以使用ChatMessageDeserializer.messageFromJson(String)和
        // ChatMessageDeserializer.messagesFromJson(String)辅助方法
        // 轻松地从JSON反序列化聊天消息。
        String json = stringRedisTemplate.opsForValue().get(REDIS_KEY_PREFIX + o.toString());
        if (json != null) {
            return ChatMessageDeserializer.messagesFromJson(json);
        } else {
            List<String> list = chatMessageService.loadHistory(Long.parseLong(o.toString()), 50);
            if (!list.isEmpty()) {
                return list.stream().map(ChatMessageDeserializer::messageFromJson).toList().reversed();
            }
        }
        return List.of();
    }

    @Override
    public void updateMessages(Object o, List<ChatMessage> list) {
        // TODO: 实现通过内存ID更新持久化存储中的所有消息。
        // 可以使用ChatMessageSerializer.messageToJson(ChatMessage)和
        // ChatMessageSerializer.messagesToJson(List<ChatMessage>)辅助方法
        // 轻松地将聊天消息序列化为JSON。
        stringRedisTemplate.opsForValue().set(REDIS_KEY_PREFIX+o.toString(), ChatMessageSerializer.messagesToJson(list), 60*60, TimeUnit.SECONDS);
        ChatMessage lastMessage = list.getLast();
        ChatMessageType type = lastMessage.type();
        Long appid = Long.parseLong(o.toString());

        {   //保存到mysql 用于恢复历史对话
            chatMessageService.saveChatMessage(appid,ChatMessageSerializer.messageToJson(lastMessage), LocalDateTime.now());
        }

        Long userId = userIdCache.get(appid,(key)->{
            App app = appMapper.selectOneById(appid);
            return app.getUserId();
        });
        switch (type) {
            case USER:{
                UserMessage userMessage = (UserMessage) lastMessage;
                Content last = userMessage.contents().getLast();
                TextContent textContent = (TextContent) last;
                chatHistoryService.addChatMessage(appid,textContent.text(),MessageTypeEnum.USER.getValue(), userId);
                break;
            }
            case AI:{
                AiMessage aiMessage = (AiMessage) lastMessage;
                if (Strings.isNotBlank(aiMessage.text())) {
                    chatHistoryService.addChatMessage(appid,aiMessage.text(),MessageTypeEnum.AI.getValue(), userId);
                }
                List<ToolExecutionRequest> toolExecutionRequests = aiMessage.toolExecutionRequests();
                if (aiMessage.hasToolExecutionRequests()) {
                    for (int i = 0; i < toolExecutionRequests.size(); i++) {
                        String name = toolExecutionRequests.get(i).name();
                        BaseTool tool = toolManager.getTool(name);
                        String result = tool.generateToolRequestResponse();
                        chatHistoryService.addChatMessage(appid,result,MessageTypeEnum.AI.getValue(), userId);
                    }
                }
            break;
            }
        }



    }

    @Override
    public void deleteMessages(Object o) {
        // TODO: 实现通过内存ID删除持久化存储中的所有消息。
        stringRedisTemplate.delete(REDIS_KEY_PREFIX+o.toString());
    }
}
