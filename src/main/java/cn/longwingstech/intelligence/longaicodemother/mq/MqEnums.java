package cn.longwingstech.intelligence.longaicodemother.mq;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MqEnums {
    EMAIL("xExchage","emailQueue","XEQ"),
    SCREENSHOTS("xExchage","screenshotsQueue","XSQ");

    final String exchange;
    final String queue;
    final String routingKey;
}
