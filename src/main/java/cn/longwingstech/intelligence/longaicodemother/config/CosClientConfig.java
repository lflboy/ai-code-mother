package cn.longwingstech.intelligence.longaicodemother.config;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.endpoint.UserSpecifiedEndpointBuilder;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.TransferManager;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Data
public class CosClientConfig {

    @Resource
    private CosConfigProperties cosConfigProperties;


    @Bean
    public COSClient cosClient() {
        COSCredentials cred = new BasicCOSCredentials(cosConfigProperties.getSecretId(),cosConfigProperties.getSecretKey());
        // 2 设置 bucket 的地域, COS 地域的简称请参见 https://cloud.tencent.com/document/product/436/6224
        // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        Region region = new Region(cosConfigProperties.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        // 这里建议设置使用 https 协议

        // 从 5.6.54 版本开始，默认使用了 https
        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 配置源地址
        String serviceApiEndpoint = "service.cos.myqcloud.com";
        UserSpecifiedEndpointBuilder endPointBuilder =
                new UserSpecifiedEndpointBuilder(cosConfigProperties.getEndpoint(), serviceApiEndpoint);
        clientConfig.setEndpointBuilder(endPointBuilder);

        // 3 生成 cos 客户端。
        return new COSClient(cred, clientConfig);
    }

    @Bean
    public TransferManager transferManager(@Qualifier("cosClient") COSClient cosClient) {
        // 使用高级接口
        ExecutorService threadPool = Executors.newFixedThreadPool(cosConfigProperties.getThreadPool()==null?1:cosConfigProperties.getThreadPool());
        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        return new TransferManager(cosClient, threadPool);
    }
}
