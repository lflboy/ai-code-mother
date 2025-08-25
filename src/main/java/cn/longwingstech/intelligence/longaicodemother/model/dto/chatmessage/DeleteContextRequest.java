package cn.longwingstech.intelligence.longaicodemother.model.dto.chatmessage;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用的删除请求类
 */
@Data
public class DeleteContextRequest implements Serializable {

    /**
     * id
     */
    private Long appId;

    private static final long serialVersionUID = 1L;
}