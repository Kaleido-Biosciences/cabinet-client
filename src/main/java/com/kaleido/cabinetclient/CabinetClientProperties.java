/*
 * Copyright (c) 2019. Kaleido Biosciences. All Rights Reserved
 */

package com.kaleido.cabinetclient;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Holds properties from externalized configuration such as environment variables or {@code application.properties} files.
 * Any variable beginning with {@code cabinet.client} or for environment variables {@code CABINET_CLIENT} and matching
 * a field of this class will over-ride the default value. For example, {@code cabinet.client.password=secret} will set
 * the value of the password used by the client.
 * Default values are set to interact with the {@code dev} profile of Cabinet and are suitable for development and testing
 * purposes.
 */
@ConfigurationProperties("cabinet.client")
@EnableRetry
public class CabinetClientProperties {

    /*
     * The properties username, password and base assume a localhost server where those credentials would be expected
     * to work (or would be overridden using standard Spring externalization of configuration methods). By switching the
     * values of username, password and the base URL you can develop against a local host test server, remote test server
     * using the same code base that you would then deploy to production and connect to a production server. Each time
     * you only need to over-ride the relevant variables using a environment statements such as
     * 'export CABINET_CLIENT_USERNAME=my_user_name' 'export CABINET_CLIENT_PASSWORD=my_password'
     * CABINET_CLIENT_BASE=https://myserver.com/api/
     */
    private String username = "admin";
    private String password = "admin";
    private String base = "http://localhost:8080/api/";

    private String searchPathComponent = "_search";

    private String authorityEndpoint = "authorities";
    private String persistentAuditEventEndpoint = "persistent-audit-events";
    private String cabinetPlateMapEndpoint = "plate-maps";
    private String userEndpoint = "users";

    private long retryInterval = 5000L;
    private double retryMultiplier = 2.0D;
    private long maxRetryInterval = 15000L;
    private int maxRequestAttempts = 3;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    public String getAuthorityEndpoint() {
        return authorityEndpoint;
    }

    public void setAuthorityEndpoint(String authorityEndpoint) {
        this.authorityEndpoint = authorityEndpoint;
    }

    public String getPersistentAuditEventEndpoint() {
        return persistentAuditEventEndpoint;
    }

    public void setPersistentAuditEventEndpoint(String persistentAuditEventEndpoint) {
        this.persistentAuditEventEndpoint = persistentAuditEventEndpoint;
    }

    public String getCabinetPlateMapEndpoint() {
        return cabinetPlateMapEndpoint;
    }

    public String getUserEndpoint() {
        return userEndpoint;
    }

    public void setUserEndpoint(String userEndpoint) {
        this.userEndpoint = userEndpoint;
    }

    public String getSearchPathComponent() {
        return searchPathComponent;
    }

    public void setSearchPathComponent(String searchPathComponent) {
        this.searchPathComponent = searchPathComponent;
    }

    public long getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }

    public double getRetryMultiplier() {
        return retryMultiplier;
    }

    public void setRetryMultiplier(double retryMultiplier) {
        this.retryMultiplier = retryMultiplier;
    }

    public long getMaxRetryInterval() {
        return maxRetryInterval;
    }

    public void setMaxRetryInterval(long maxRetryInterval) {
        this.maxRetryInterval = maxRetryInterval;
    }

    public int getMaxRequestAttempts() {
        return maxRequestAttempts;
    }

    public void setMaxRequestAttempts(int maxRequestAttempts) {
        this.maxRequestAttempts = maxRequestAttempts;
    }


}
