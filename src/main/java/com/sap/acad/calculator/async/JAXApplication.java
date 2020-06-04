package com.sap.acad.calculator.async;

import org.glassfish.jersey.server.ResourceConfig;

import javax.servlet.annotation.HttpConstraint;
import javax.ws.rs.ApplicationPath;

@ApplicationPath("calculator")
public class JAXApplication extends ResourceConfig {

    public JAXApplication() {
        this.register(AsyncCalculator.class);
    }
}

