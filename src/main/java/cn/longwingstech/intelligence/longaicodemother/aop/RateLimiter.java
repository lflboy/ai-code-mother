package cn.longwingstech.intelligence.longaicodemother.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimiter {
    /**
     * 限流key前缀
     */
    String key();
    
    /**
     * 限流时间,单位秒
     */
    int time() default 60;
    
    /**
     * 限流次数
     */
    int count() default 100;
    
    /**
     * 限流类型 目前只实现了 IP
     */
    LimitType limitType() default LimitType.IP;
}