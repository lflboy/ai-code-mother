package cn.longwingstech.intelligence.longaicodemother.config;

import cn.hutool.core.io.FileUtil;
import cn.longwingstech.intelligence.longaicodemother.common.constant.FileConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

// 实现WebMvcConfigurer接口
@Configuration
// @EnableWebMvc  可能会覆盖默认配置，建议注释
public class WebMvcConfig implements WebMvcConfigurer {

    // 重写addResourceHandlers方法
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String ROOT_PATH = System.getProperty("user.dir");
        FileUtil.mkdir(ROOT_PATH + File.separatorChar + FileConstants.ROOT_PATH);
        registry.addResourceHandler("/webjars/**").addResourceLocations(
                    "classpath:/META-INF/resources/webjars/");
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/img/**").addResourceLocations("file:" + ROOT_PATH + File.separatorChar + FileConstants.ROOT_PATH);
    }
    
}

// registry.addResourceHandler("/img/**")设置服务前缀
// .addResourceLocations("file:" + UPLOAD_FOLDER); 设置本地地址	
