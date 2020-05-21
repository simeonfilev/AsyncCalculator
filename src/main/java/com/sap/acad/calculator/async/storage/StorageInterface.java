package com.sap.acad.calculator.async.storage;

import com.sap.acad.calculator.async.exceptions.StorageException;
import com.sap.acad.calculator.async.models.Expression;

import java.util.List;

public interface StorageInterface{

    int saveExpression(Expression expression) throws StorageException;

    List<Expression> getExpressions() throws StorageException;

    void deleteExpressionById(int id) throws StorageException;

    void deleteLastRowExpression() throws StorageException;

    boolean getStatusOfExpression(int id) throws StorageException;

    void calculateNotCalculatedExpressions() throws StorageException;

    List<Expression> getNotCalculatedExpressions() throws StorageException;

    Expression getExpressionByID(int id) throws StorageException;

    void deleteInvalidExpressions() throws StorageException;
}
