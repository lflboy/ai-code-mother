package cn.longwingstech.intelligence.longaicodemother.aop;

public enum LimitType {
    /**
     * 默认策略，根据IP限流
     */
    IP,
    /**
     * 根据方法名限流
     */
    METHOD,
    /**
     * 根据用户ID限流
     */
    USER
}