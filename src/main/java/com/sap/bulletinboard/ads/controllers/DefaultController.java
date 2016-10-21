package com.sap.bulletinboard.ads.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
public class DefaultController {

    @GetMapping
    public String get(@RequestHeader("Authorization") String authorization ) {
         //TODO DO NOT EXPOSE THIS DATA IN PRODUCTION!!!
//         return "<!DOCTYPE HTML>" +
//             "<html>" +
//             "<head></head>" +
//             "<body>" +
//             "Please decode the JWT token <a href = \"https://jwt.io\" target=\"_new\">here</a><br><br>" +
//             authorization +
//             "</body>" +
//             "</html>";
        return "OK";
    }
    
    @GetMapping("/instance-index")
    public String getIndex(@Value("${CF_INSTANCE_INDEX}") String instanceIndex) {
        return "Instance index: " + instanceIndex;
    }
}