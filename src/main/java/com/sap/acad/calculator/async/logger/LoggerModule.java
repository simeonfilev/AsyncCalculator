package com.sap.acad.calculator.async.logger;

import com.google.inject.AbstractModule;
import com.google.inject.matcher.Matchers;
import com.sap.acad.calculator.async.annotations.Loggable;

public class LoggerModule extends AbstractModule {

    @Override
    protected void configure() {
        bindInterceptor(Matchers.any(),
                Matchers.annotatedWith(Loggable.class),
                new LoggerInterceptor());
    }
}
