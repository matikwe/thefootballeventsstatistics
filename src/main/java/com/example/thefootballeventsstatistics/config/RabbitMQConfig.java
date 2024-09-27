package com.example.thefootballeventsstatistics.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.example.thefootballeventsstatistics.strings.UtilityStrings.*;

@Configuration
public class RabbitMQConfig {

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        rabbitTemplate.setReplyTimeout(5000);
        return rabbitTemplate;
    }

    @Bean
    public Queue resultQueue() {
        return new Queue(RESULT_QUEUE, true);
    }

    @Bean
    public Queue statisticsQueue() {
        return new Queue(STATISTICS_QUEUE, true);
    }

    @Bean
    public Queue replyQueue() {
        return new Queue(REPLY_QUEUE, false);
    }

    @Bean
    public TopicExchange eventsExchange() {
        return new TopicExchange(EVENT_EXCHANGE);
    }

    @Bean
    public Binding resultBinding(Queue resultQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(resultQueue).to(eventsExchange).with(RESULT);
    }

    @Bean
    public Binding statisticsBinding(Queue statisticsQueue, TopicExchange eventsExchange) {
        return BindingBuilder.bind(statisticsQueue).to(eventsExchange).with(STATISTICS);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
}