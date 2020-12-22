/*
 * Copyright (c) 2019. Kaleido Biosciences. All Rights Reserved
 */

package com.kaleido.cabinetclient.authentication;

import com.kaleido.cabinetclient.CabinetClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * Intercepts calls to the Cabinet api (only) and injects an {@code Authorization} header. It will attempt an authentication
 * if the user is not yet authenticated.
 */
public class CabinetJWTRequestInterceptor implements ClientHttpRequestInterceptor {

    private CabinetUserCredentials cabinetUserCredentials;
    private String CabinetBase;

    private Logger log = LoggerFactory.getLogger(CabinetJWTRequestInterceptor.class);

    public CabinetJWTRequestInterceptor(CabinetUserCredentials cabinetUserCredentials, CabinetClientProperties CabinetClientProperties){
        this.cabinetUserCredentials = cabinetUserCredentials;
        this.CabinetBase = CabinetClientProperties.getBase();
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes, ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {

        //if the request is to the Cabinet service we should add the authorization token
        if(httpRequest.getURI().toString().contains(CabinetBase)) {

            log.debug("Intercepting call to {}, setting authorization header", httpRequest.getURI().toString());
            //set the token on the header
            httpRequest.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + cabinetUserCredentials.getBearerToken());
        }

        //execute the client request
        return clientHttpRequestExecution.execute(httpRequest, bytes);
    }
}
