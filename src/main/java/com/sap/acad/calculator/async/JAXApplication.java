package com.sap.acad.calculator.async;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sap.acad.calculator.async.logger.LoggerModule;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("calculator")
public class JAXApplication extends ResourceConfig {

    public JAXApplication() {
        Injector injector = Guice.createInjector(new LoggerModule());
        AsyncCalculator restCalculator = injector.getInstance(AsyncCalculator.class);
        this.register(restCalculator);
    }
}

