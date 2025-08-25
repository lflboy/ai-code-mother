package cn.longwingstech.intelligence.longaicodemother.config;

import cn.longwingstech.intelligence.longaicodemother.monitor.AiModelMonitorListener;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.time.Duration;
import java.util.List;

@Data
@Configuration
@ConfigurationProperties("langchain4j.prototype-streaming-chat-model")
public class PrototypeStreamingChatModelConfig {
    private String apiKey;
    private String baseUrl;
    private String modelName;
    private Boolean logRequests;
    private Boolean logResponses;
    private Integer maxTokens;
    private Double temperature;
    @Bean("prototypeStreamingChatModel")
    @Scope("prototype")
    public StreamingChatModel prototypeStreamingChatModel(AiModelMonitorListener aiModelMonitorListener) {
        return OpenAiStreamingChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(modelName)
                .logRequests(logRequests)
                .logResponses(logResponses)
                .maxTokens(maxTokens)
                .temperature(temperature)
                .timeout(Duration.ofMinutes(15))
                .listeners(List.of(aiModelMonitorListener))
                .build();
     }
}
