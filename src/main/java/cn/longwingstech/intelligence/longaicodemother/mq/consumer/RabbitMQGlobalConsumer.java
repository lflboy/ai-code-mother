package cn.longwingstech.intelligence.longaicodemother.mq.consumer;


import cn.hutool.json.JSONUtil;
import cn.longwingstech.intelligence.longaicodemother.model.entity.App;
import cn.longwingstech.intelligence.longaicodemother.mq.dto.EmailMqMessage;
import cn.longwingstech.intelligence.longaicodemother.mq.dto.ScreenshotsMqDTO;
import cn.longwingstech.intelligence.longaicodemother.service.AppService;
import cn.longwingstech.intelligence.longaicodemother.service.ScreenshotService;
import cn.longwingstech.intelligence.longaicodemother.utils.EmailUtil;
import cn.longwingstech.intelligence.longaicodemother.utils.WebScreenshotUtils;
import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.amqp.core.Message;
import org.springframework.util.Assert;

import java.io.IOException;

/**
 * rabbitmq 全局消费者
 */
@Slf4j
@Component
public class RabbitMQGlobalConsumer {
    @Resource
    private AppService appService;
    @Resource
    private ScreenshotService screenshotService;

    @Resource
    private EmailUtil emailUtil;
    @RabbitListener(queues = "emailQueue")
    public void onEmailMessage(Message message, Channel channel) throws IOException {
        String messageStr = new String(message.getBody());
        EmailMqMessage emailMqMessage = JSONUtil.toBean(messageStr, EmailMqMessage.class);
        log.info("邮箱消费：{}",emailMqMessage);
        try {
            emailUtil.sendEmail(emailMqMessage.getEmail(),emailMqMessage.getTitle(),emailMqMessage.getTitle());
            log.info("发送邮件成功，收件用户：{}",emailMqMessage.getEmail());
        } catch (MessagingException e) {
            log.error("发送邮件失败，收件用户：{}",emailMqMessage.getEmail());
            throw new RuntimeException(e);
        }
    }

    @RabbitListener(queues = "screenshotsQueue")
    public void onScreenshotsMessage(Message message, Channel channel) throws IOException {
        String json = new String(message.getBody());
        ScreenshotsMqDTO screenshotsMqDTO = JSONUtil.toBean(json, ScreenshotsMqDTO.class);
        String screenshot = screenshotService.generateAndUploadScreenshot(screenshotsMqDTO.getWebUrl());
        App entity = new App();
        entity.setId(screenshotsMqDTO.getAppid());
        entity.setCover(screenshot);

        boolean result = appService.updateById(entity);

        Assert.state(result, "更新失败");
    }
}
