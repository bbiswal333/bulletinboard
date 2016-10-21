package com.sap.bulletinboard.ads.services;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.netflix.hystrix.HystrixCommandKey;
import com.sap.hcp.cf.logging.common.LogContext;
import com.sap.xs2.security.container.SecurityContext;
import com.sap.xs2.security.container.UserInfo;
import com.sap.xs2.security.container.UserInfoException;

@Component // defines a Spring Bean with name "statisticsServiceClient"
public class StatisticsServiceClient {
    private static final String ROUTING_KEY = "statistics.adIsShown";

    private Logger logger = LoggerFactory.getLogger(getClass());

    private RabbitTemplate rabbitTemplate;

    @Inject
    public StatisticsServiceClient(AmqpAdmin amqpAdmin, RabbitTemplate rabbitTemplate) {
        amqpAdmin.declareQueue(new Queue(ROUTING_KEY)); // creates queue, if not existing
        this.rabbitTemplate = rabbitTemplate;
    }

    public void advertisementIsShown(long id) throws UserInfoException {
        new IncrementCounterCommand(id).queue(); // queue calls the run() asynchronously
    }

    private class IncrementCounterCommand extends HystrixCommand<Void> {
        protected final String correlationId;
        private Long id;
        private UserInfo userInfo;
        private static final String TENANT = "tenant";

        IncrementCounterCommand(Long id) throws UserInfoException {
            super(Setter.withGroupKey(HystrixCommandGroupKey.Factory.asKey(StatisticsServiceClient.class.getName()))
                    .andCommandKey(HystrixCommandKey.Factory.asKey(ROUTING_KEY)));
            this.id = id;
            this.correlationId = LogContext.getCorrelationId();
            this.userInfo = SecurityContext.getUserInfo();
        }

        @Override
        protected Void run() throws Exception {
            String tenant = userInfo.getIdentityZone();
            LogContext.initializeContext(correlationId);
            LogContext.add(TENANT, tenant);

            logger.info("sending message '{}' for routing key '{}'", id, ROUTING_KEY);
            
            rabbitTemplate.convertAndSend(ROUTING_KEY, id, new MessagePostProcessor() {
                public Message postProcessMessage(Message message) {
                    message.getMessageProperties().setCorrelationId(LogContext.getCorrelationId());
                    message.getMessageProperties().setHeader(TENANT, tenant);
                    return message;
                }
            });
            return null;
        }

        @Override
        protected Void getFallback() {
            logger.warn("Failure to send message to statistics service");
            return null;
        }
    }
}