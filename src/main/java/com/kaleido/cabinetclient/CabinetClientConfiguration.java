/*
 * Copyright (c) 2019. Kaleido Biosciences. All Rights Reserved
 */

package com.kaleido.cabinetclient;

import com.kaleido.cabinetclient.authentication.CabinetJWTRequestInterceptor;
import com.kaleido.cabinetclient.authentication.UserCredentials;
import com.kaleido.cabinetclient.client.CabinetClient;
import com.kaleido.cabinetclient.client.CabinetClientHTTPException;
import com.kaleido.cabinetclient.client.CabinetResponseErrorHandler;
import com.kaleido.cabinetclient.domain.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the {@code @Beans} needed for the Cabinet client. Applications using this library should include this class
 * in the list of scanned classes or packages. {@code @Beans} in this class are configured through externalized configuration
 * such as an {@code application.properties} file via the {@code CabinetClientProperties} class
 * {@see CabinetClientProperties}
 */
@SpringBootApplication
@EnableConfigurationProperties(CabinetClientProperties.class)
public class CabinetClientConfiguration {

    private CabinetClientProperties cabinetClientProperties;


    public CabinetClientConfiguration(CabinetClientProperties cabinetClientProperties) {
        this.cabinetClientProperties = cabinetClientProperties;
    }

    @Bean
    @Primary
    RestTemplate restTemplate(CabinetJWTRequestInterceptor cabinetJWTRequestInterceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(cabinetJWTRequestInterceptor);
        restTemplate.setErrorHandler(new CabinetResponseErrorHandler());
        return restTemplate;
    }

    /**
     * Configuration for Cabinet Client Retry template.  When a request returns an exception related to 502 or 503
     * the service can automatically retry up to a predefined amount of times (Default: 3 including original call)
     * after an incremental amount of wait time (Default 5 seconds, doubling each attempt with default max of 15 seconds)
     * The following CabinetClientProperties can be changed to change the behavior of the retryTemplate
     * <p>
     * retryInterval: The initial delay, in milliseconds, before the request is retried (default: 5000L)
     * retryMultiplier: The value that the delay interval will be multiplied by before each attempt (default: 2.0D)
     * For example an initial delay of 5000ms at multiplier 2.0, the second attempt would wait 10000ms
     * and the third attempt would wait 20000ms.
     * maxRetryInterval: The max delay, in milliseconds, that the retry would wait before retrying. (default: 15000L)
     * maxRequestAttempts: The max amount of times a request can be called.  This value is inclusive of the original attempt.
     * Setting this value to 1 would effectively disable retry.
     **/
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        ExponentialBackOffPolicy exponentialBackOffPolicy = new ExponentialBackOffPolicy();
        exponentialBackOffPolicy.setInitialInterval(cabinetClientProperties.getRetryInterval());
        exponentialBackOffPolicy.setMultiplier(cabinetClientProperties.getRetryMultiplier());
        exponentialBackOffPolicy.setMaxInterval(cabinetClientProperties.getMaxRetryInterval());
        retryTemplate.setBackOffPolicy(exponentialBackOffPolicy);
        Map<Class<? extends Throwable>, Boolean> includeExceptions = new HashMap<>();
        includeExceptions.put(CabinetClientHTTPException.CabinetClientGatewayTimeoutException.class, true);
        includeExceptions.put(CabinetClientHTTPException.CabinetClientBadGatewayException.class, true);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy(cabinetClientProperties.getMaxRequestAttempts(), includeExceptions);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }

    @Bean
    CabinetJWTRequestInterceptor cabinetJWTRequestInterceptor(UserCredentials userCredentials) {
        return new CabinetJWTRequestInterceptor(userCredentials, cabinetClientProperties);
    }

    @Bean
    CabinetClient<Authority> authorityClient(RestTemplate restTemplate, RetryTemplate retryTemplate) {
        return new CabinetClient<>(cabinetClientProperties.getBase() + cabinetClientProperties.getAuthorityEndpoint(),
                cabinetClientProperties.getBase() +
                        cabinetClientProperties.getSearchPathComponent() + "/"
                        + cabinetClientProperties.getAuthorityEndpoint(),
                restTemplate, retryTemplate, Authority.class);
    }

    @Bean
    CabinetClient<PersistentAuditEvent> persistentAuditEventClient(RestTemplate restTemplate, RetryTemplate retryTemplate) {
        return new CabinetClient<>(cabinetClientProperties.getBase() + cabinetClientProperties.getPersistentAuditEventEndpoint(),
                cabinetClientProperties.getBase() +
                        cabinetClientProperties.getSearchPathComponent() + "/"
                        + cabinetClientProperties.getPersistentAuditEventEndpoint(),
                restTemplate, retryTemplate, PersistentAuditEvent.class);
    }


    @Bean
    CabinetClient<PlateMap> plateMapClient(RestTemplate restTemplate, RetryTemplate retryTemplate) {
        return new CabinetClient<>(cabinetClientProperties.getBase() + cabinetClientProperties.getPlateMapEndpoint(),
                cabinetClientProperties.getBase() +
                        cabinetClientProperties.getSearchPathComponent() + "/"
                        + cabinetClientProperties.getPlateMapEndpoint(),
                restTemplate, retryTemplate, PlateMap.class);
    }

    @Bean
    CabinetClient<User> userClient(RestTemplate restTemplate, RetryTemplate retryTemplate) {
        return new CabinetClient<>(cabinetClientProperties.getBase() + cabinetClientProperties.getUserEndpoint(),
                cabinetClientProperties.getBase() +
                        cabinetClientProperties.getSearchPathComponent() + "/"
                        + cabinetClientProperties.getUserEndpoint(),
                restTemplate, retryTemplate, User.class);
    }



}
