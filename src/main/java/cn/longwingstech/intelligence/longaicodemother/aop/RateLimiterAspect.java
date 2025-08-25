package cn.longwingstech.intelligence.longaicodemother.aop;

import cn.longwingstech.intelligence.longaicodemother.utils.IpUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
/**
 * 自定义限流器
 */
public class RateLimiterAspect {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final String LOCK_KEY_PREFIX = "rate_limiter:";
    @Around("@annotation(rateLimiter)")
    public Object around(ProceedingJoinPoint joinPoint,RateLimiter rateLimiter) throws Throwable {

        String key = LOCK_KEY_PREFIX + generateKey(joinPoint, rateLimiter);
        int time = rateLimiter.time();
        int count = rateLimiter.count();

        stringRedisTemplate.opsForValue().setIfAbsent(key, "0", time, TimeUnit.SECONDS);
        Long increment = stringRedisTemplate.opsForValue().increment(key);
        Assert.state(increment <= count, "服务繁忙");
        return joinPoint.proceed();
    }
    private String generateKey(ProceedingJoinPoint joinPoint, RateLimiter rateLimiter) {
        StringBuilder key = new StringBuilder(rateLimiter.key());
        LimitType limitType = rateLimiter.limitType();

        switch (limitType) {
            case IP:
                key.append(IpUtils.getIpAddr());
                break;
            case USER:
                // 这里需要根据你的用户系统获取当前用户ID
                // key.append(getCurrentUserId());
                throw new UnsupportedOperationException("User based rate limiting not implemented yet");
            case METHOD:
                key.append(joinPoint.getSignature().toShortString());
                break;
            default:
                key.append(IpUtils.getIpAddr());
        }

        return key.toString();
    }

}
