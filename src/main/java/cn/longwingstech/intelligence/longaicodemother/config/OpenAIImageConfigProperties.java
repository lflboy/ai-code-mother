package cn.longwingstech.intelligence.longaicodemother.config;

import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties("openai.image")
@ConditionalOnBooleanProperty(prefix = "openai.image", name = "enabled", havingValue = true)
public class OpenAIImageConfigProperties {
    private String apiKey;
    private String baseUrl;
    private String imageModel;
    private String imageSize;
    private Boolean enabled;
}
