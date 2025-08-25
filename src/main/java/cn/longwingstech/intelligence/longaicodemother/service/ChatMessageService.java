package cn.longwingstech.intelligence.longaicodemother.service;

import cn.longwingstech.intelligence.longaicodemother.model.entity.ChatHistory;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import cn.longwingstech.intelligence.longaicodemother.model.entity.ChatMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 服务层。
 *
 * @author 君墨
 */
public interface ChatMessageService extends IService<ChatMessage> {
public Long saveChatMessage(Long appId, String message, LocalDateTime createDate);

    /**
     * 加载聊天记录
     * @param limit
     * @return
     */
    public List<String> loadHistory(Long appid,int limit);

}
