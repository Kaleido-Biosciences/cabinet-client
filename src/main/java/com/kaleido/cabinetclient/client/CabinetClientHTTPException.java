/*
 * Copyright (c) 2019. Kaleido Biosciences. All Rights Reserved
 */

package com.kaleido.cabinetclient.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;

public class CabinetClientHTTPException extends HttpServerErrorException {

    private Logger log = LoggerFactory.getLogger(CabinetClientHTTPException.class);

    public CabinetClientHTTPException(HttpStatus statusCode) {
        super(statusCode);
    }

    public CabinetClientHTTPException(HttpStatus statusCode, String statusText) {
        super(statusCode, statusText);
    }

    public static class CabinetClientGatewayTimeoutException extends CabinetClientHTTPException {
        CabinetClientGatewayTimeoutException(HttpStatus statusCode) {
            super(statusCode);
        }

        CabinetClientGatewayTimeoutException(HttpStatus statusCode, String statusText) {
            super(statusCode, statusText);
        }
    }

    public static class CabinetClientBadGatewayException extends CabinetClientHTTPException {
        CabinetClientBadGatewayException(HttpStatus statusCode) {
            super(statusCode);
        }

        CabinetClientBadGatewayException(HttpStatus statusCode, String statusText) {
            super(statusCode, statusText);
        }
    }
}
