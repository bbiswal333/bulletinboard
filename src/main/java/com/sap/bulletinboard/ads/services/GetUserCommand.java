package com.sap.bulletinboard.ads.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.HystrixCommand;
import com.netflix.hystrix.HystrixCommandGroupKey;
import com.sap.bulletinboard.ads.services.UserServiceClient.User;

public class GetUserCommand extends HystrixCommand<User> {
    // Hystrix uses a default timeout of 1000 ms, increase in case you run into problems in remote locations
    private static final int DEFAULT_TIMEOUT_MS = 1000;
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String url;
    private RestTemplate restTemplate;

    public GetUserCommand(String url, RestTemplate restTemplate) {
        super(HystrixCommandGroupKey.Factory.asKey("User"), DEFAULT_TIMEOUT_MS);
        this.url = url;
        this.restTemplate = restTemplate;
    }

    @Override
    protected User run() throws Exception {
        logger.info("sending request {}", url);

        ResponseEntity<User> responseEntity = sendRequest();

        HttpStatus statusCode = responseEntity.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            logger.warn("received HTTP status code: {}", statusCode);
            throw new IllegalStateException("Unsuccessful outgoing request");
        }
        logger.info("received response, status code: {}", statusCode);
        return responseEntity.getBody();
    }

    protected ResponseEntity<User> sendRequest() {
        return restTemplate.getForEntity(url, User.class);
    }

    // this will be used in exercise 18
    protected int getTimeoutInMs() {
        return this.properties.executionTimeoutInMilliseconds().get();
    }
}
