package cn.longwingstech.intelligence.longaicodemother.manager;

import cn.longwingstech.intelligence.longaicodemother.config.CosConfigProperties;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * COS 对象存储管理器
 */
@Component
@Slf4j
public class CosManager {

    @Resource
    private CosConfigProperties cosConfigProperties;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key  唯一键
     * @param file 文件
     * @return 上传结果
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfigProperties.getBucket(), key, file);
        return cosClient.putObject(putObjectRequest);
    }

    /**
     * 上传文件到 COS 并返回访问 URL
     *
     * @param key  COS对象键（完整路径）
     * @param file 要上传的文件
     * @return 文件的访问URL，失败返回null
     */
    public String uploadFile(String key, File file) {
        PutObjectResult result = putObject(key, file);
        if (result != null) {
            String url = String.format("%s/%s", cosConfigProperties.getHost(), key);
            log.info("文件上传到 COS 成功：{} -> {}", file.getName(), url);
            return url;
        } else {
            log.error("文件上传到 COS 失败：{}，返回结果为空", file.getName());
            return null;
        }
    }

    /**
     * 上传文件到 COS
     * @param key
     * @param in 文件输入流
     * @return
     */
    public String uploadFile(String key, InputStream in) {
        PutObjectResult result = cosClient.putObject(cosConfigProperties.getBucket(), key, in, null);
        if (result != null) {
            String url = String.format("%s/%s", cosConfigProperties.getHost(), key);
            log.info("文件上传到 COS 成功：{} -> {}", key, url);
            return url;
        } else {
            log.error("文件上传到 COS 失败：{}，返回结果为空", key);
            return null;
        }
    }

    /**
     * 获取临时访问签名
     * @param key 文件名称
     * @param expireTime 签名过期时间 单位秒
     * @return
     */
    public String generatePresignedUrl(String key,Integer expireTime) {
        // 这里设置签名在半个小时后过期
        Date expirationDate = new Date(System.currentTimeMillis() + expireTime * 1000);
        // 填写本次请求的参数，需与实际请求相同，能够防止用户篡改此签名的 HTTP 请求的参数
        Map<String, String> params = new HashMap<String, String>();
        params.put("key", key);
        // 填写本次请求的头部，需与实际请求相同，能够防止用户篡改此签名的 HTTP 请求的头部
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("header", key);
        // 请求的 HTTP 方法，上传请求用 PUT，下载请求用 GET，删除请求用 DELETE
        HttpMethodName method = HttpMethodName.GET;
        URL url = cosClient.generatePresignedUrl(cosConfigProperties.getBucket(), key, expirationDate, method, headers, params);
        System.err.println(url.toString());
        return url.toString();
    }

    /**
     * 获取下载流
     * @param key
     * @return
     */
    public COSObjectInputStream getDownloadStream(String key) {
        return cosClient.getObject(cosConfigProperties.getBucket(), key).getObjectContent();
    }

    // 获取图
    public COSObject getObject(String key) {
        return cosClient.getObject(cosConfigProperties.getBucket(), key);
    }

}
