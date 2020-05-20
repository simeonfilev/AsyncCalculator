package com.sap.acad.calculator.async;

import com.sap.acad.calculator.async.exceptions.StorageException;
import com.sap.acad.calculator.async.models.Expression;
import com.sap.acad.calculator.async.storage.StorageInterface;
import com.sap.acad.calculator.async.storage.mysql.MySQLStorageImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.http.HttpServlet;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/expressions")
public class AsyncCalculator extends HttpServlet {

    private static final String JSON_EXPRESSION_ID = "id";
    private static final String JSON_EXPRESSION_EXPRESSION = "expression";
    private static final String JSON_EXPRESSION_ANSWER = "answer";
    private static final String JSON_EXPRESSION_CALCULATED = "calculated";
    private static final String JSON_EXPRESSIONS = "expressions";

    private static final Logger logger = LogManager.getLogger(AsyncCalculator.class);
    private StorageInterface storage = new MySQLStorageImpl();

    @GET
    public Response getHistory() {
        JSONObject json = getJSONObjectFromExpressionArray();
        return okRequestGetHistoryGET(json);
    }

    @GET
    @Path("/status")
    public Response getIsCalculated(@QueryParam("id") Integer id){
        if (id == null || id.toString().length() == 0) {
            return noContentFoundToDeleteExpression();
        }
        try {
            boolean status =  storage.getStatusOfExpression(id);
            if(status)
                return calculatedResponseToGetStatus();

        } catch (StorageException e) {
            logger.error("Couldn't connect to storage");
            logger.error(e.getMessage(), e);
        }
        return notCalculatedResponseToGetStatus();
    }


    @POST
    public Response saveExpression(@QueryParam("expression") String expression) {
        if (expression == null || expression.trim().length() == 0) {
            return invalidExpressionResponsePOST();
        }
        try {
            storage.saveExpression(new Expression(expression));
            return correctExpressionResponsePOST();
        }catch (StorageException exception) {
            logger.error("Couldn't connect to storage");
            logger.error(exception.getMessage(), exception);
        }
        return badRequestResponsePOST();
    }

    @DELETE
    public Response deleteExpression(@QueryParam("id") Integer id) {
        if (id == null || id.toString().length() == 0) {
            return noContentFoundToDeleteExpression();
        }
        try {
            storage.deleteExpressionById(id);
            return okResponseToDeleteExpression();
        } catch (StorageException e) {
            logger.error("Couldn't connect to storage");
            logger.error(e.getMessage(), e);
        }
        return noContentFoundToDeleteExpression();
    }


    public Response noContentFoundToDeleteExpression() {
        return Response.status(Response.Status.NO_CONTENT)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .allow()
                .type(MediaType.APPLICATION_JSON)
                .entity("")
                .build();
    }

    public Response okResponseToDeleteExpression() {
        return Response.status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .allow()
                .type(MediaType.APPLICATION_JSON)
                .entity("")
                .build();
    }

    public Response calculatedResponseToGetStatus() {
            return Response.status(Response.Status.OK)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET")
                    .allow()
                    .build();

    }

    public Response notCalculatedResponseToGetStatus() {
        return Response.status(Response.Status.ACCEPTED)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .allow()
                .build();
    }


    public Response okRequestGetHistoryGET(JSONObject json) {
        return Response.status(Response.Status.OK)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET")
                .allow()
                .type(MediaType.APPLICATION_JSON)
                .entity(json.toString())
                .build();
    }

    public Response badRequestResponsePOST() {
        return Response.status(Response.Status.BAD_REQUEST)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST")
                .allow()
                .entity("Couldn't save expression")
                .build();
    }

    public Response correctExpressionResponsePOST() {
        return Response.status(Response.Status.CREATED)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST")
                .allow()
                .entity("Saved Expression in storage Correctly")
                .build();
    }

    public Response invalidExpressionResponsePOST() {
        return Response.status(Response.Status.BAD_REQUEST)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST")
                .allow()
                .entity("Invalid Expression")
                .build();
    }

    public JSONObject getJSONObjectFromExpressionArray() {
        List<Expression> expressions;
        try {
            expressions = storage.getExpressions();
        } catch (StorageException exception) {
            logger.error(exception.getMessage(), exception);
            return new JSONObject();
        }
        JSONObject json = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for (Expression expression : expressions) {
            JSONObject tempExpression = new JSONObject();
            tempExpression.put(JSON_EXPRESSION_ID, expression.getId());
            tempExpression.put(JSON_EXPRESSION_EXPRESSION, expression.getExpression());
            tempExpression.put(JSON_EXPRESSION_ANSWER, expression.getAnswer());
            tempExpression.put(JSON_EXPRESSION_CALCULATED,expression.isCalculated());
            jsonArray.put(tempExpression);
        }
        json.put(JSON_EXPRESSIONS, jsonArray);
        return json;
    }

}


