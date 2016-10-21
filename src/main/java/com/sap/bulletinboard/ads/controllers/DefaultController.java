package com.sap.bulletinboard.ads.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/")
@RestController
public class DefaultController {

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public String get(@RequestHeader("Authorization") String authorization) {
        // TODO DO NEVER EXPOSE THIS DATA IN PRODUCTION!!!
//        String BEARER = "Bearer";
//
//        if (!authorization.isEmpty() && authorization.startsWith(BEARER)) {
//
//            String tokenContent = authorization.replaceFirst(BEARER, "").trim();
//
//            // Decode JWT token
//            Jwt decodedJwt = JwtHelper.decode(tokenContent);
//
//            return decodedJwt.getClaims();
//        }
        return JSONObject.quote("OK");
    }

    @GetMapping("/instance-index")
    public String getIndex(@Value("${CF_INSTANCE_INDEX}") String instanceIndex) {
        return "Instance index: " + instanceIndex;
    }
}