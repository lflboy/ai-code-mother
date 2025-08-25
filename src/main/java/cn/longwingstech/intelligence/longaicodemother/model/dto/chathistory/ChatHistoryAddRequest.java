package cn.longwingstech.intelligence.longaicodemother.model.dto.chathistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 对话历史添加请求
 *
 * @author 君墨
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryAddRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 消息内容
     */
    private String message;

    /**
     * 消息类型：user/ai/error
     */
    private String messageType;

    /**
     * 应用id
     */
    private Long appId;
}