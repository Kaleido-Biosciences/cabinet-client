/*
 * Copyright (c) 2019. Kaleido Biosciences. All Rights Reserved
 */

package com.kaleido.cabinetclient.authentication;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(properties = {  //set up the test with these defined "external" configured variables
        "Cabinet.client.username=admin",
        "Cabinet.client.password=test",
        "Cabinet.client.base=http://localhost:8080/"})
public class CabinetUserCredentialsTest {

    @Autowired
    private CabinetUserCredentials cabinetUserCredentials;

    @Test
    public void usernameShouldBeInjected() {
        assertEquals("username should be injected", "admin", cabinetUserCredentials.getUsername());
    }

    @Test
    public void passwordShouldBeInjected() {
        assertEquals("password should be injected", "test", cabinetUserCredentials.getPassword());
    }
}