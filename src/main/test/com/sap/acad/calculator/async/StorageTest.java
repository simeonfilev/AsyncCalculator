package com.sap.acad.calculator.async;

import com.sap.acad.calculator.Calculator;
import com.sap.acad.calculator.async.exceptions.StorageException;
import com.sap.acad.calculator.async.models.Expression;
import com.sap.acad.calculator.async.storage.StorageInterface;
import com.sap.acad.calculator.async.storage.mysql.MySQLStorageImpl;
import org.hsqldb.server.Server;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static java.sql.DriverManager.getConnection;

public class StorageTest {


    @Test
    public void StorageInterfaceIsWorkingCorrectly() throws StorageException {
        List<Expression> expressions = new ArrayList<>();
        Calculator calculator = new Calculator();
        StorageInterface storage = new StorageInterface() {
            @Override
            public int saveExpression(Expression expression) {
                expressions.add(expression);
                return expression.getId();
            }

            @Override
            public List<Expression> getExpressions() {
                return expressions;
            }

            @Override
            public void deleteExpressionById(int id) {
                expressions.remove(id);
            }

            @Override
            public void deleteLastRowExpression() {
                expressions.remove(expressions.size()-1);
            }

            @Override
            public boolean getStatusOfExpression(int id){
                return expressions.get(id).isCalculated();
            }

            @Override
            public void calculateNotCalculatedExpressions(){
                for(Expression expression : expressions){
                    if(!expression.isCalculated()){
                        expression.setAnswer(calculator.calculate(expression.getExpression()));
                        expression.setCalculated(true);
                    }
                }
            }

            @Override
            public List<Expression> getNotCalculatedExpressions(){
                List<Expression> notCalculatedExpressions = new ArrayList<>();
                for(Expression expression : expressions){
                    if(!expression.isCalculated()){
                        notCalculatedExpressions.add(expression);
                    }
                }
                return notCalculatedExpressions;
            }

            @Override
            public Expression getExpressionByID(int id) {
                List<Expression> expressions = getExpressions();
                for (Expression expression : expressions) {
                    if (expression.getId() == id) {
                        return expression;
                    }
                }
                return null;
            }
        };

        Assertions.assertEquals(expressions.size(),0);

        storage.saveExpression(new Expression("2+5",7.0));
        storage.saveExpression(new Expression("2+1",3.0));
        storage.saveExpression(new Expression("2+2",4.0));
        Assertions.assertEquals(3,expressions.size());

        storage.deleteLastRowExpression();
        Assertions.assertNotEquals("2+2",expressions.get(expressions.size()-1).getExpression());
        Assertions.assertEquals(2,expressions.size());

        storage.deleteExpressionById(0);
        Assertions.assertNotEquals("2+5",expressions.get(0).getExpression());
        Assertions.assertEquals(1,expressions.size());

        Assertions.assertEquals("2+1",expressions.get(0).getExpression());
    }
    @Test
    public void mySQLBasedStorageIsWorkingCorrectly() throws StorageException {
        MySQLStorageImpl storage = new MySQLStorageImpl();
        List<Expression> expressionList = storage.getExpressions();
        int startingCount = expressionList.size();

        storage.saveExpression(new Expression("2+5", 7.0));
        Assertions.assertEquals(startingCount + 1, storage.getExpressions().size(), "Successfully added expression to database");

        storage.saveExpression(new Expression("2+6", 8.0));
        storage.deleteLastRowExpression();
        Assertions.assertEquals(storage.getExpressions().get(storage.getExpressions().size() - 1).getExpression(), "2+5", "Successfully removed last expression");
        Assertions.assertEquals(startingCount + 1, storage.getExpressions().size());

        while (startingCount != storage.getExpressions().size()) {
            storage.deleteLastRowExpression();
        }

    }

    @Test
    public void isCalculatingNotCalculatedExpressions() throws StorageException, InterruptedException {
        MySQLStorageImpl storage = new MySQLStorageImpl();
        Expression expression = new Expression("2+5");
        int id = storage.saveExpression(expression);
        Thread.sleep(150); // wait 1 sec for job to finish (CalculateNotCalculatedExpressions JOB : every 100 mil seconds)
        Expression newExpression = storage.getExpressionByID(id);
        Assertions.assertTrue(newExpression.isCalculated());
        storage.deleteExpressionById(id);
    }


    @Test
    public void inMemoryDatabaseSQLIsWorkingCorrectly() throws ClassNotFoundException, SQLException, StorageException {
        MySQLStorageImpl spyStorage = Mockito.spy(MySQLStorageImpl.class);
        Server server = new Server();
        server.start();


        String url = "jdbc:hsqldb:mem:mymemdb;shutdown=false";
        Connection c = getConnection(url);
        Mockito.when(spyStorage.getConnection()).thenReturn(c);

        // SET UP Table
        c.prepareStatement("CREATE TABLE expressions(id INTEGER IDENTITY PRIMARY KEY,expression  varchar(255),answer double, calculated boolean); ").execute();

        Mockito.when(spyStorage.getConnection()).thenReturn(DriverManager.getConnection(url));
        Assertions.assertEquals(spyStorage.getExpressions().size(), 0);

        Mockito.when(spyStorage.getConnection()).thenReturn(DriverManager.getConnection(url));
        spyStorage.saveExpression(new Expression("15+5"));

        Mockito.when(spyStorage.getConnection()).thenReturn(DriverManager.getConnection(url));
        boolean isCalculated = spyStorage.getStatusOfExpression(1);
        Assertions.assertFalse(isCalculated);

        Mockito.when(spyStorage.getConnection()).thenReturn(DriverManager.getConnection(url));
        Assertions.assertEquals(spyStorage.getExpressions().size(), 1);


        Mockito.when(spyStorage.getConnection()).thenReturn(DriverManager.getConnection(url));
        spyStorage.deleteExpressionById(0);

        Mockito.when(spyStorage.getConnection()).thenReturn(DriverManager.getConnection(url));
        Assertions.assertEquals(spyStorage.getExpressions().size(), 0);

        server.stop();
    }


}
