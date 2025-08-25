package cn.longwingstech.intelligence.longaicodemother;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("cn.longwingstech.intelligence.longaicodemother.mapper")
@EnableCaching
public class LongAiCodeMotherApplication {
    public static void main(String[] args) {
        SpringApplication.run(LongAiCodeMotherApplication.class, args);
    }

}
