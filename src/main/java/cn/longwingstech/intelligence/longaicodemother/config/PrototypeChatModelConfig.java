package cn.longwingstech.intelligence.longaicodemother.config;

import cn.longwingstech.intelligence.longaicodemother.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.ChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties("langchain4j.prototype-chat-model")
public class PrototypeChatModelConfig {
    private String apiKey;
    private String baseUrl;
    private String modelName;
    private Boolean logRequests;
    private Boolean logResponses;
    private Integer maxTokens;
    private Double temperature;
    private Integer maxRetries;

    @Bean("prototypeChatModel")
    @Scope("prototype")
    public ChatModel prototypeChatModel() {
        return dev.langchain4j.model.openai.OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName(modelName)
                .baseUrl(baseUrl)
                .temperature(temperature)
                .maxTokens(maxTokens)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .maxRetries(maxRetries)
                .timeout(Duration.ofMinutes(15))
                .build();
    }
}
