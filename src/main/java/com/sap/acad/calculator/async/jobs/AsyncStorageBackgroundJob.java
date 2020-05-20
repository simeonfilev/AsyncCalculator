package com.sap.acad.calculator.async.jobs;

import com.sap.acad.calculator.async.storage.StorageInterface;
import com.sap.acad.calculator.async.storage.mysql.MySQLStorageImpl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class AsyncStorageBackgroundJob implements ServletContextListener {
    private ScheduledExecutorService scheduler;
    private StorageInterface storage = new MySQLStorageImpl();

    @Override
    public void contextInitialized(ServletContextEvent event) {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new CalculateNotCalculatedExpressionsJob(storage), 0, 3, TimeUnit.SECONDS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        scheduler.shutdownNow();
    }

}
