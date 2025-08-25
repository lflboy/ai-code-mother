package cn.longwingstech.intelligence.longaicodemother.utils;

import jakarta.annotation.Resource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Slf4j
@Component
public class EmailUtil {

    @Resource
    private JavaMailSender javaMailSender;

    public void sendEmail(String email,String title,String content) throws MessagingException, UnsupportedEncodingException {
        // 创建一个邮件消息
        MimeMessage message = javaMailSender.createMimeMessage();

        // 创建 MimeMessageHelper
        MimeMessageHelper helper = new MimeMessageHelper(message, false);

        // 发件人邮箱和名称
        helper.setFrom("a13695191730@163.com", "AI智能体平台");
        // 收件人邮箱
        helper.setTo(email);
        // 邮件标题
        helper.setSubject(title);
        // 邮件正文，第二个参数表示是否是HTML正文
        helper.setText(content);
        // 发送
        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            log.error("邮件发送失败：{}", e.getMessage());
        }
    }

    /**
     * 发送html格式邮箱
     * @param email
     * @param title
     * @param content
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    public void sendHtmlEmail(String email,String title,String content) throws MessagingException, UnsupportedEncodingException {
        // 创建一个邮件消息
        MimeMessage message = javaMailSender.createMimeMessage();

        // 创建 MimeMessageHelper
        MimeMessageHelper helper = new MimeMessageHelper(message, false);

        // 发件人邮箱和名称
        helper.setFrom("a13695191730@163.com", "AI智能体平台");
        // 收件人邮箱
        helper.setTo(email);
        // 邮件标题
        helper.setSubject(title);
        // 邮件正文，第二个参数表示是否是HTML正文
        helper.setText(content, true);
        // 发送
        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            log.error("邮件发送失败：{}", e.getMessage());
        }
    }
}
