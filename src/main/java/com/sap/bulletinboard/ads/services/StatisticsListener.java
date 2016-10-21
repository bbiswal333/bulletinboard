package com.sap.bulletinboard.ads.services;

import java.nio.charset.Charset;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component // defines a Spring Bean with name "statisticsListener"
@Profile("cloud") // should not be loaded in tests
public class StatisticsListener implements MessageListener {
    private static final Charset CHARSET_UTF8 = Charset.forName("UTF-8");
    private static final String QUEUE_NAME = "statistics.periodicalStatistics";

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    public StatisticsListener(AmqpAdmin amqpAdmin, ConnectionFactory rabbitConnectionFactory) {
        amqpAdmin.declareQueue(new Queue(QUEUE_NAME));

        logger.info("registering as listener for for queue '{}'", QUEUE_NAME);
        SimpleMessageListenerContainer listenerContainer = new SimpleMessageListenerContainer();
        listenerContainer.setConnectionFactory(rabbitConnectionFactory);
        listenerContainer.setQueueNames(QUEUE_NAME);
        listenerContainer.setMessageListener(this);
        listenerContainer.start();
    }

    @Override
    public void onMessage(Message message) {
        logger.info("got statistics: {}", toString(message.getBody()));
    }

    private String toString(byte[] byteArray) {
        return new String(byteArray, CHARSET_UTF8);
    }
}