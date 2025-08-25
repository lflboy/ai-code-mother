package cn.longwingstech.intelligence.longaicodemother.mq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class TtlConfig {


    @Bean("xExchange")
    public Exchange xExchange() {
        return new DirectExchange(MqEnums.EMAIL.getExchange());
    }

    @Bean("emailQueue")
    public Queue emailQueue() {
        return QueueBuilder.durable(MqEnums.EMAIL.getQueue()).build();
    }

    @Bean("screenshotsQueue")
    public Queue screenshotsQueue() {
        return QueueBuilder.durable(MqEnums.SCREENSHOTS.getQueue()).build();
    }
    @Bean("xExchangeBindScreenshotsQueue")
    public Binding xExchangeBindScreenshotsQueue(@Qualifier("xExchange") Exchange xExchange
            , @Qualifier("screenshotsQueue") Queue screenshotsQueue) {

        return BindingBuilder.bind(screenshotsQueue).to(xExchange).with(MqEnums.SCREENSHOTS.getRoutingKey()).noargs();
    }
    @Bean("xExchangeBindEmailQueue")
    public Binding xExchangeBindEmailQueue(@Qualifier("xExchange") Exchange xExchange
            , @Qualifier("emailQueue") Queue emailQueue) {

        return BindingBuilder.bind(emailQueue).to(xExchange).with(MqEnums.EMAIL.getRoutingKey()).noargs();
    }

}
