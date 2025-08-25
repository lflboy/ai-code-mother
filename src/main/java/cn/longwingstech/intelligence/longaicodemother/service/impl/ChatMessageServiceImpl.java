package cn.longwingstech.intelligence.longaicodemother.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import cn.longwingstech.intelligence.longaicodemother.model.entity.ChatMessage;
import cn.longwingstech.intelligence.longaicodemother.mapper.ChatMessageMapper;
import cn.longwingstech.intelligence.longaicodemother.service.ChatMessageService;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层实现。
 *
 * @author 君墨
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    @Override
    public Long saveChatMessage(Long appId, String message, LocalDateTime createDate) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setAppId(appId);
        chatMessage.setMessage(message);
        chatMessage.setCreateTime(createDate);
        boolean result = super.save(chatMessage);
        Assert.state(result, "保存消息失败");
        return chatMessage.getId();
    }

    /**
     * 加载聊天记录
     *
     * @param limit
     * @return
     */
    @Override
    public List<String> loadHistory(Long appid, int limit) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq(ChatMessage::getAppId, appid)
                .orderBy(ChatMessage::getCreateTime, false)
                .limit(limit);
        List<ChatMessage> list = super.list(queryWrapper);
        return list.stream().map(ChatMessage::getMessage).toList();
    }
}
