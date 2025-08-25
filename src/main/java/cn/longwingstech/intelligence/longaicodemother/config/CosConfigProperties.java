package cn.longwingstech.intelligence.longaicodemother.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("cos.client")
@Data
public class CosConfigProperties {
    private String host;
    private String endpoint;
    private String region;
    private String secretId;
    private String secretKey;
    private Integer threadPool;
    private String bucket;
}
