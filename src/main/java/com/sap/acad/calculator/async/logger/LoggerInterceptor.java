package com.sap.acad.calculator.async.logger;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.logging.log4j.LogManager;

public class LoggerInterceptor implements MethodInterceptor {

    private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(LoggerInterceptor.class);

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        StringBuilder arguments = new StringBuilder();
        for(var s : invocation.getArguments()){
           arguments.append(s);
        }
        logger.debug("Starting method: " + invocation.getMethod().getName() + " with args: "+arguments);
        return invocation.proceed();
    }
}

