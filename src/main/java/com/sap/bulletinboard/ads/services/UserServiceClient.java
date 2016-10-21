package com.sap.bulletinboard.ads.services;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component // defines a Spring Bean with name "userServiceClient"
public class UserServiceClient {
    private static final String PATH = "api/v1.0/users";
    private final RestTemplate restTemplate;

    // Using Spring's PropertySourcesPlaceholderConfigurer bean, get the content of the USER_ROUTE environment variable
    @Value("${USER_ROUTE}")
    private String userServiceRoute;
    private Logger logger;

    @Inject
    public UserServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    public boolean isPremiumUser(String id) throws RuntimeException {
        String url = userServiceRoute + "/" + PATH + "/" + id;
        logger.info("sending request {}", url);
        
        ResponseEntity<User> responseEntity = restTemplate.getForEntity(url, User.class);

        HttpStatus statusCode = responseEntity.getStatusCode();
        if (!statusCode.is2xxSuccessful()) {
            logger.warn("received HTTP status code: {}", statusCode);
            throw new IllegalStateException("Unsuccessful outgoing request");
        }
        logger.info("received response, status code: {}", statusCode);
        return responseEntity.getBody().premiumUser;
    }

    public static class User {
        // public, so that Jackson can access the field
        public boolean premiumUser;
    }
}