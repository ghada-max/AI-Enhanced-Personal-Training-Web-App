package com.fittness.activity.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {


    @Bean
    public Queue activityQueue(){
        return new Queue("activities.queue",true);
    }

    @Bean
    public MessageConverter jsonMessageConverter(){
        return new JacksonJsonMessageConverter();
    }
    @Bean
    public Binding activityBinding(Queue activitiesQueue,
                                   DirectExchange fitnessExchange) {

        return BindingBuilder
                .bind(activitiesQueue)
                .to(fitnessExchange)
                .with("activity.tracking");
    }

    @Bean
    public DirectExchange activityExchange()
    {
        return new DirectExchange("fitness.exchange");}

}

