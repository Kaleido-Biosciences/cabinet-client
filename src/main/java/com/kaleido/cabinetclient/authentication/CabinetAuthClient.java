/*
 * Copyright (c) 2019. Kaleido Biosciences. All Rights Reserved
 */

package com.kaleido.cabinetclient.authentication;

import com.kaleido.cabinetclient.CabinetClientProperties;
import com.kaleido.cabinetclient.client.CabinetRestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Specialized Client for the /authorization endpoint of Cabinet. It authenticates the user and obtains a JWT token
 * for subsequent calls.
 */
@Service
@EnableConfigurationProperties({CabinetClientProperties.class})
public class CabinetAuthClient {
    private CabinetRestTemplate cabinetRestTemplate;
    private String serviceUri;

    public static final String AUTH_ENDPOINT = "authenticate";

    Logger log = LoggerFactory.getLogger(CabinetAuthClient.class);

    public CabinetRestTemplate getCabinetRestTemplate() {
        return cabinetRestTemplate;
    }

    public CabinetAuthClient(CabinetClientProperties CabinetClientProperties) {
        this.serviceUri = CabinetClientProperties.getBase();

        /*
         * important to declare a new CabinetRestTemplate here so that we don't get the one with the JWTInterceptor which will
         * attempt to add a token that we don't have yet
         */
        this.cabinetRestTemplate = new CabinetRestTemplate();
    }

    /**
     * Calls the authentication endpoint of Cabinet to obtain a token
     * @param cabinetUserCredentials a Pojo with the username and password of the user
     * @return an object wrapping the JWT token.
     */
    public UserToken getUserToken(CabinetUserCredentials cabinetUserCredentials){

        log.info("getting user token from {}", serviceUri+AUTH_ENDPOINT);
        UserToken userToken = cabinetRestTemplate.postForObject(serviceUri + AUTH_ENDPOINT, cabinetUserCredentials, UserToken.class);

        if( StringUtils.hasText(userToken.getBearer()) ){
            log.info("obtained token");
            log.trace("user token: {}", userToken);
        } else {
            log.warn("User token not obtained");
        }

        return userToken;
    }
}
