package com.messageforge.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String MESSAGE_QUEUE = "messageforge.queue.send";
    public static final String MESSAGE_EXCHANGE = "messageforge.exchange.send";
    public static final String MESSAGE_ROUTING_KEY = "messageforge.routing.send";

    public static final String DEAD_LETTER_QUEUE = "messageforge.queue.dlq";
    public static final String DEAD_LETTER_EXCHANGE = "messageforge.exchange.dlq";
    public static final String DEAD_LETTER_ROUTING_KEY = "messageforge.routing.dlq";

    @Value("${RABBITMQ_HOST:localhost}")
    private String host;

    @Value("${RABBITMQ_PORT:5672}")
    private int port;

    @Value("${RABBITMQ_USERNAME:guest}")
    private String username;

    @Value("${RABBITMQ_PASSWORD:guest}")
    private String password;

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host, port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }

    @Bean
    public Queue messageQueue() {
        return QueueBuilder.durable(MESSAGE_QUEUE)
                .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public DirectExchange messageExchange() {
        return new DirectExchange(MESSAGE_EXCHANGE);
    }

    @Bean
    public Binding messageBinding() {
        return BindingBuilder.bind(messageQueue())
                .to(messageExchange())
                .with(MESSAGE_ROUTING_KEY);
    }

    @Bean
    public Queue deadLetterQueue() {
        return new Queue(DEAD_LETTER_QUEUE);
    }

    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
