package com.miempresa.erp.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // @Value("${spring.rabbitmq.host:rabbitmq.rabbitmq-system.svc.cluster.local}")
    // private String host;

    // @Value("${spring.rabbitmq.port:5672}")
    // private int port;

    // @Value("${spring.rabbitmq.username:admin}")
    // private String username;

    // @Value("${spring.rabbitmq.password:newpassword}")
    // private String password;

    // @Value("${erp.rabbitmq.exchange:erp-exchange}")
    // private String exchange;

    // Tablas a sincronizar del modelo proporcionado

    @Value("${spring.rabbitmq.host:157.230.182.118}")
    private String host;

    @Value("${spring.rabbitmq.port:30673}")
    private int port;

    @Value("${spring.rabbitmq.username:user}")
    private String username;

    @Value("${spring.rabbitmq.password:password}")
    private String password;

    @Value("${erp.rabbitmq.exchange:erp-exchange}")
    private String exchange;

    private final String[] SYNC_TABLES = { "user", "solicitude", "offer", "loan", "monthly_payment" };

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        return factory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public TopicExchange erpExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("ml-dead-letter").build();
    }

    @Bean
    public Binding deadLetterBinding(Queue deadLetterQueue, TopicExchange erpExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(erpExchange).with("error.*");
    }
}
