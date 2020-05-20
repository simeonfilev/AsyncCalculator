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

    private static final String DB_URL = System.getenv("DB_URL");
    private static final String DB_USERNAME = System.getenv("DB_USERNAME");
    private static final String DB_PASSWORD = System.getenv("DB_PASSWORD");

    private static final String SQL_DELETE_LAST_EXPRESSION = "DELETE FROM expressions ORDER BY id DESC LIMIT 1;";
    private static final String SQL_GET_ALL_EXPRESSIONS = "SELECT * FROM expressions;";
    private static final String SQL_SAVE_EXPRESSION = "INSERT INTO expressions(expression) VALUES (?);";
    private static final String SQL_DELETE_EXPRESSION_WITH_ID = "DELETE FROM expressions WHERE id = ?;";
    private static final String SQL_SET_CALCULATED_TRUE_BY_ID = "UPDATE expressions SET calculated=true WHERE id = ?;";
    private static final String SQL_SET_ANSWER_BY_ID = "UPDATE expressions SET answer= ? WHERE id = ?;";
    private static final String SQL_GET_STATUS_BY_ID = "SELECT * FROM expressions WHERE id = ?;";

    public MySQLStorageImpl() {

    }

    @Override
    public void saveExpression(Expression expression) throws StorageException {
        logger.debug("Connecting to database...");
        try (Connection connection = getConnection();
             PreparedStatement statement = getPreparedStatement(SQL_SAVE_EXPRESSION, connection)) {
            logger.debug("Connected to database! Saving expression:" + expression);
            statement.setString(1, expression.getExpression());
            statement.execute();
            logger.debug("Successfully saved expression: " + expression);
        } catch (SQLException | ClassNotFoundException e) {
            throw new StorageException(e.getMessage(), e);
        }
    }

    @Override
    public List<Expression> getExpressions() throws StorageException {
        List<Expression> expressions;
        logger.debug("Connecting to database...");
        try (Connection connection = getConnection();
             PreparedStatement statement = getPreparedStatement(SQL_GET_ALL_EXPRESSIONS, connection);
             ResultSet rs = statement.executeQuery()) {
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
             PreparedStatement statement = getPreparedStatement(SQL_GET_STATUS_BY_ID, connection)) {
            logger.debug("Connected to database! And trying to get expression with id:" + id);
            statement.setInt(1, id);
            ResultSet rs = statement.executeQuery();
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
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
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
                statementSetAnswer.setDouble(1, calculator.calculate(expression.getExpression()));
                statementSetAnswer.setInt(2, expression.getId());
                statementSetAnswer.executeUpdate();
                statementSetStatus.setInt(1, expression.getId());
                statementSetStatus.executeUpdate();
            } catch (SQLException | ClassNotFoundException e) {
                throw new StorageException(e.getMessage(), e);
            }
        }

    }
}
