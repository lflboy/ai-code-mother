package cn.longwingstech.intelligence.longaicodemother.ai;

import cn.longwingstech.intelligence.longaicodemother.utils.SpringContextUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 提示词验证AI服务创建工厂
 */
@Slf4j
@Component
public class PromptAuthServiceFactory {
    /**
     * AI 服务实例缓存
     * 缓存策略：
     * - 最大缓存 1000 个实例
     * - 写入后 30 分钟过期
     * - 访问后 10 分钟过期
     */
    private final Cache<String, PromptAuthService> serviceCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(30))
            .expireAfterAccess(Duration.ofMinutes(10))
            .removalListener((key, value, cause) -> {
                log.debug("AI 服务实例被移除，缓存键: {}, 原因: {}", key, cause);
            })
            .build();


    public PromptAuthService create(String appid) {
        return serviceCache.get(appid, keys -> {
       ChatModel prototypeChatModel = SpringContextUtil.getBean("prototypeChatModel", ChatModel.class);
        return AiServices.builder(PromptAuthService.class)
                .chatModel(prototypeChatModel)
                .build();
        });
    }
}
