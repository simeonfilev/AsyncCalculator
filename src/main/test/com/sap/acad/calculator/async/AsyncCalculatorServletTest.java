package com.sap.acad.calculator.async;

import com.google.gson.Gson;
import com.sap.acad.calculator.async.exceptions.StorageException;
import com.sap.acad.calculator.async.storage.StorageInterface;
import com.sap.acad.calculator.async.storage.mysql.MySQLStorageImpl;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AsyncCalculatorServletTest extends JerseyTest {

    private final Gson gson = new Gson();
    private StorageInterface storage = new MySQLStorageImpl();

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected Application configure() {
        return new ResourceConfig(AsyncCalculator.class);
    }

    private boolean isValidJSON(String jsonInString) {
        try {
            gson.fromJson(jsonInString, Object.class);
            return true;
        } catch (com.google.gson.JsonSyntaxException ex) {
            return false;
        }
    }


    @Test
    public void returnCorrectJSONToHistoryRequest() {
        var req = target("/expressions/all");
        Response response = req.request().get();
        String json = response.readEntity(String.class);
        Assertions.assertTrue(isValidJSON(json), "Does not return valid JSON");
        assertEquals("Http Response should be 200: ", Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("Content type should be JSON: ", MediaType.APPLICATION_JSON, response.getMediaType().toString());
    }


    @Test
    public void returnCorrectResponseToExpressionToServletGetRequest() {
        String expression = "5+2*3";
        var req = target("/expressions/").queryParam("expression", expression);
        Response response = req.request().post(Entity.text(""));
        assertEquals("Http Response should be 200: ", Response.Status.CREATED.getStatusCode(), response.getStatus());
        try {
            storage.deleteLastRowExpression(); //delete expression from history
        } catch (StorageException e) {
            Assertions.fail("Couldn't delete from database");
        }
    }


    @Test
    public void returnsStatusToGetRequest() {
        Integer id = 3;
        var req = target("/expressions/status/").queryParam("id", id);
        Response response = req.request().get();
        assertTrue(Response.Status.ACCEPTED.getStatusCode() == response.getStatus()
                || Response.Status.OK.getStatusCode() == response.getStatus());
    }

}
