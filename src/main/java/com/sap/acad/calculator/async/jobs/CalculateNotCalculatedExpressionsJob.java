package com.sap.acad.calculator.async.jobs;

import com.sap.acad.calculator.async.exceptions.StorageException;
import com.sap.acad.calculator.async.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CalculateNotCalculatedExpressionsJob implements Runnable {

    private StorageInterface storage;
    private static final Logger logger = LogManager.getLogger(CalculateNotCalculatedExpressionsJob.class);

    public CalculateNotCalculatedExpressionsJob(StorageInterface storage) {
        this.storage = storage;
    }

    @Override
    public void run() {
        try {
            storage.calculateNotCalculatedExpressions();
        } catch (StorageException e) {
            logger.error(e.getMessage(),e);
        }
    }

}
