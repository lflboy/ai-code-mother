package cn.longwingstech.intelligence.longaicodemother.mq.dto;

import cn.longwingstech.intelligence.longaicodemother.utils.WebScreenshotUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 截图请求消息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScreenshotsMqDTO {
    private Long appid;
    // 网页地址
    private String webUrl;

}
