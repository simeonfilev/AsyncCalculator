package com.sap.acad.calculator.async;

import com.google.gson.Gson;
import com.sap.acad.calculator.async.exceptions.StorageException;
import com.sap.acad.calculator.async.models.Expression;
import com.sap.acad.calculator.async.storage.StorageInterface;
import com.sap.acad.calculator.async.storage.mysql.MySQLStorageImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/expressions")
public class AsyncCalculator extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(AsyncCalculator.class);
    private StorageInterface storage = new MySQLStorageImpl();

    @GET
    @Path("/all")
    public Response getHistory() {
        String json = getJSONObjectFromExpressionArray();
        return buildResponse(200,json);
    }

    @GET
    public Response getExpressionByID(@QueryParam("id") Integer id){
        if (id == null || id.toString().length() == 0) {
            return buildResponse(204,"");
        }
        try {
            Expression expression =  storage.getExpressionByID(id);
            if(expression!= null){
                return okCalculatedExpression(expression);
            }
            return buildResponse(404,"");
        } catch (StorageException e) {
            logger.error("Couldn't connect to storage");
            logger.error(e.getMessage(), e);
        }
        return buildResponse(204,"");
    }

    @GET
    @Path("/status")
    public Response getIsCalculated(@QueryParam("id") Integer id){
        if (id == null || id.toString().length() == 0) {
            return buildResponse(204,"");
        }
        try {
            boolean status =  storage.getStatusOfExpression(id);
            if(status){
                return buildResponse(200,"");
            }
            if(storage.getExpressionByID(id) == null){
                return buildResponse(404,"");
            }
        } catch (StorageException e) {
            logger.error("Couldn't connect to storage");
            logger.error(e.getMessage(), e);
        }
        return buildResponse(404,"");
    }


    @POST
    public Response saveExpression(@QueryParam("expression") String expressionString) {
        if (expressionString == null || expressionString.trim().length() == 0) {
            return buildResponse(404,"");
        }
        try {
            Expression expression = new Expression(expressionString);
            if(expression.isValidExpression()){
                int id  = storage.saveExpression(expression);
                if(id != -1)
                    return correctExpressionResponsePOST(id);
            }else{
                return buildResponse(400,"");
            }

        }catch (StorageException exception) {
            logger.error("Couldn't connect to storage");
            logger.error(exception.getMessage(), exception);
        }
        return buildResponse(204,"");
    }

    @DELETE
    public Response deleteExpression(@QueryParam("id") Integer id) {
        if (id == null || id.toString().length() == 0) {
            return buildResponse(204,"");
        }
        try {
            storage.deleteExpressionById(id);
            return buildResponse(200,"");
        } catch (StorageException e) {
            logger.error("Couldn't connect to storage");
            logger.error(e.getMessage(), e);
        }
        return buildResponse(204,"");
    }

    public Response okCalculatedExpression(Expression expression) {
        String jsonString = new Gson().toJson(expression);
        return buildResponse(200,jsonString);
    }

    public Response buildResponse(int code,String response){
        return Response.status(code)
                .type(MediaType.APPLICATION_JSON)
                .entity(response)
                .build();
    }

    public Response correctExpressionResponsePOST(int id) {
        String jsonString = new Gson().toJson(id);
        return buildResponse(201,jsonString);
    }

    public String getJSONObjectFromExpressionArray() {
        List<Expression> expressions;
        Gson gson = new Gson();
        try {
            expressions = storage.getExpressions();
            return gson.toJson(expressions);
        } catch (StorageException exception) {
            logger.error(exception.getMessage(), exception);
            return "";
        }
    }
}



