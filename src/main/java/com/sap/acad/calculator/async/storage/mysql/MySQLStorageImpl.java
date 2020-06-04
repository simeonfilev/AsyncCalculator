package com.sap.acad.calculator.async.storage.mysql;

import com.sap.acad.calculator.Calculator;
import com.sap.acad.calculator.async.exceptions.StorageException;
import com.sap.acad.calculator.async.models.Expression;
import com.sap.acad.calculator.async.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MySQLStorageImpl implements StorageInterface {
    private static final Logger logger = LogManager.getLogger(MySQLStorageImpl.class);

    private static final String DB_USERNAME = System.getenv("DB_USER");
    private static final String DB_PASSWORD =System.getenv("DB_PASSWORD");
    private static final String DB_DRIVER =System.getenv("DB_DRIVER");
    private static final String DB_URL = System.getenv("DB_URL");

    private static final String SQL_TABLE_NAME = "EXPRESSIONS";
    private static final String SQL_CREATE_TABLE = "CREATE TABLE EXPRESSIONS(\"ID\" bigint NOT NULL primary key GENERATED BY DEFAULT AS IDENTITY,expression  varchar(255),answer double, calculated boolean,username varchar(255));"; //<--HANA |||| MYSQL ->"CREATE TABLE `EXPRESSIONS` (`id` int NOT NULL AUTO_INCREMENT,`expression` varchar(255) DEFAULT NULL,`answer` double DEFAULT NULL,`calculated` tinyint(1) DEFAULT NULL,`user` varchar(255) DEFAULT NULL,PRIMARY KEY (`id`) ) ";
    private static final String SQL_DELETE_LAST_EXPRESSION = "DELETE FROM EXPRESSIONS ORDER BY id DESC LIMIT 1;";
    private static final String SQL_GET_ALL_EXPRESSIONS = "SELECT * FROM EXPRESSIONS;";
    private static final String SQL_GET_ALL_EXPRESSIONS_BY_USER = "SELECT * FROM EXPRESSIONS WHERE username = ?;";
    private static final String SQL_SAVE_EXPRESSION = "INSERT INTO EXPRESSIONS(expression,username) VALUES (?,?);";
    private static final String SQL_DELETE_EXPRESSION_WITH_ID = "DELETE FROM EXPRESSIONS WHERE id = ?;";
    private static final String SQL_SET_CALCULATED_TRUE_BY_ID = "UPDATE EXPRESSIONS SET calculated=true WHERE id = ?;";
    private static final String SQL_SET_ANSWER_BY_ID = "UPDATE EXPRESSIONS SET answer= ? WHERE id = ?;";
    private static final String SQL_GET_EXPRESSION_BY_ID = "SELECT * FROM EXPRESSIONS WHERE id = ?;";
    private static final String SQL_GET_ID_OF_INSERTED_EXPRESSION = "SELECT * FROM EXPRESSIONS WHERE expression=? ORDER BY id DESC LIMIT 1";

    public MySQLStorageImpl() {
        try {
            createTableIfItDoesntExist();
        }catch (StorageException e){
            logger.error(e.getMessage(),e);
        }
    }

    private void createTable() throws StorageException {
        try (Connection connection = getConnection();
             PreparedStatement createTableStatement = getPreparedStatement(SQL_CREATE_TABLE,connection)){
                createTableStatement.executeUpdate();
                logger.debug("Connected to database!");
                logger.debug("Successfully created table");
        } catch (SQLException | ClassNotFoundException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    private void createTableIfItDoesntExist() throws StorageException{
        try (Connection connection = getConnection()){
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, SQL_TABLE_NAME, null);
            if (!tables.next()) {
                createTable();
            }

        } catch (SQLException | ClassNotFoundException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    @Override
    public int saveExpression(Expression expression) throws StorageException {
        logger.debug("Connecting to database...");
        try (Connection connection = getConnection();
             PreparedStatement statement = getPreparedStatement(SQL_SAVE_EXPRESSION, connection)) {
            logger.debug("Connected to database! Saving expression:" + expression);
            statement.setString(1, expression.getExpression());
            statement.setString(2, expression.getUsername());
            statement.execute();
            try( PreparedStatement getIdStatement = getPreparedStatement(SQL_GET_ID_OF_INSERTED_EXPRESSION, connection)){
                getIdStatement.setString(1,expression.getExpression());
                ResultSet rs = getIdStatement.executeQuery();
                if(rs.next()){
                    logger.debug("Successfully saved expression: " + expression);
                    return rs.getInt(1);
                }
            }
            logger.debug("Successfully saved expression: " + expression);
        } catch (SQLException | ClassNotFoundException e) {
            throw new StorageException(e.getMessage(), e);
        }
        return -1;
    }

    @Override
    public List<Expression> getExpressions(String username) throws StorageException {
        List<Expression> expressions;
        logger.debug("Connecting to database...");
        try (Connection connection = getConnection();
             PreparedStatement statement = getPreparedStatement(SQL_GET_ALL_EXPRESSIONS_BY_USER, connection);) {
            statement.setString(1,username);
            ResultSet rs = statement.executeQuery();
            logger.debug("Connected to database!");
            expressions = getExpressionsFromResultSet(rs);
            logger.debug("Successfully received all expressions with length:" + expressions.size());
        } catch (SQLException | ClassNotFoundException e) {
            throw new StorageException(e.getMessage(), e);
        }
        return expressions;
    }

    @Override
    public List<Expression> getExpressions() throws StorageException {
        List<Expression> expressions;
        logger.debug("Connecting to database...");
        try (Connection connection = getConnection();
             PreparedStatement statement = getPreparedStatement(SQL_GET_ALL_EXPRESSIONS, connection);) {
            ResultSet rs = statement.executeQuery();
            logger.debug("Connected to database!");
            expressions = getExpressionsFromResultSet(rs);
            logger.debug("Successfully received all expressions with length:" + expressions.size());
        } catch (SQLException | ClassNotFoundException e) {
            throw new StorageException(e.getMessage(), e);
        }
        return expressions;
    }

    public List<Expression> getExpressionsFromResultSet(ResultSet rs) throws SQLException {
        List<Expression> expressions = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt(1);
            String expressionString = rs.getString(2);
            Double answer = rs.getDouble(3);
            boolean isCalculated = rs.getBoolean(4);
            Expression expression = new Expression(id, expressionString, answer, isCalculated);
            expressions.add(expression);
        }
        return expressions;
    }

    @Override
    public List<Expression> getNotCalculatedExpressions() throws StorageException {
        List<Expression> expressions;
        logger.debug("Connecting to database...");
        try (Connection connection = getConnection();
             PreparedStatement statement = getPreparedStatement(SQL_GET_ALL_EXPRESSIONS, connection);
             ResultSet rs = statement.executeQuery()) {
            logger.debug("Connected to database!");
            expressions =  getExpressionsFromResultSet(rs).stream().filter(e -> !e.isCalculated()).collect(Collectors.toUnmodifiableList());
            logger.debug("Successfully received all not calculated expressions with length:" + expressions.size());
        } catch (SQLException | ClassNotFoundException e) {
            throw new StorageException(e.getMessage(), e);
        }
        return expressions;
    }

    @Override
    public Expression getExpressionByID(int id) throws StorageException {
        logger.debug("Connecting to database...");
        try (Connection connection = getConnection();
             PreparedStatement statement = getPreparedStatement(SQL_GET_EXPRESSION_BY_ID, connection)) {
            logger.debug("Connected to database! And trying to get expression with id:" + id);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                Expression expression = new Expression(rs.getInt(1),rs.getString(2),rs.getDouble(3),rs.getBoolean(4));
                logger.debug("Expression " + expression);
                return expression;
            } else {
                logger.debug("Expression is not existing");
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new StorageException(e.getMessage(), e);
        }
        return null;
    }


    @Override
    public void deleteExpressionById(int id) throws StorageException {
        logger.debug("Connecting to database...");
        try (Connection connection = getConnection();
             PreparedStatement statement = getPreparedStatement(SQL_DELETE_EXPRESSION_WITH_ID, connection)) {
            logger.debug("Connected to database! And trying to delete expression with id:" + id);
            statement.setInt(1, id);
            statement.executeUpdate();
            logger.debug("Successfully deleted expression with id:" + id);
        } catch (SQLException | ClassNotFoundException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    @Override
    public void deleteLastRowExpression() throws StorageException {
        logger.debug("Connecting to database...");
        try (Connection connection = getConnection();
             PreparedStatement statement = getPreparedStatement(SQL_DELETE_LAST_EXPRESSION, connection)) {
            logger.debug("Connected to database! And trying to delete last row expression");
            statement.executeUpdate();
            logger.debug("Successfully deleted last row");
        } catch (SQLException | ClassNotFoundException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    @Override
    public boolean getStatusOfExpression(int id) throws StorageException {
        logger.debug("Connecting to database...");
        try (Connection connection = getConnection();
             PreparedStatement statement = getPreparedStatement(SQL_GET_EXPRESSION_BY_ID, connection)) {
            logger.debug("Connected to database! And trying to get expression with id:" + id);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
            logger.debug("here");
            if (rs.next()) {
                logger.debug("Expression is calculated: " + rs.getBoolean(4));
                return rs.getBoolean(4);
            } else {
                logger.debug("Expression is not calculated");
            }
        } catch (SQLException | ClassNotFoundException e) {
            throw new StorageException(e.getMessage(), e);
        }
        return false;//not found
    }

    public Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName(DB_DRIVER);
        return DriverManager.getConnection(DB_URL,DB_USERNAME,DB_PASSWORD);
    }

    private PreparedStatement getPreparedStatement(String sql, Connection connection) throws SQLException {
        return connection.prepareStatement(sql);
    }

    @Override
    public void calculateNotCalculatedExpressions() throws StorageException {
        Calculator calculator = new Calculator();
        List<Expression> expressions = getNotCalculatedExpressions();
        for (Expression expression : expressions) {
            try (Connection connection = getConnection();
                 PreparedStatement statementSetAnswer = getPreparedStatement(SQL_SET_ANSWER_BY_ID, connection);
                 PreparedStatement statementSetStatus = getPreparedStatement(SQL_SET_CALCULATED_TRUE_BY_ID, connection)) {
                double answer = calculator.calculate(expression.getExpression());
                statementSetAnswer.setDouble(1, answer);
                statementSetAnswer.setInt(2, expression.getId());
                statementSetAnswer.executeUpdate();
                statementSetStatus.setInt(1, expression.getId());
                statementSetStatus.executeUpdate();

            }catch(UnsupportedOperationException | SQLException | ClassNotFoundException e){
                throw new StorageException(e.getMessage(), e);
            }
        }
    }
}
