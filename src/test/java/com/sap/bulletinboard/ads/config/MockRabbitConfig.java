package com.sap.bulletinboard.ads.config;

import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MockRabbitConfig {
    @Bean
    AmqpTemplate amqpTemplate() {
        return Mockito.mock(AmqpTemplate.class);
    }

    @Bean
    AmqpAdmin amqpAdmin() {
        return Mockito.mock(AmqpAdmin.class);
    }
}