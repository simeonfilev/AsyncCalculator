package com.sap.acad.calculator.async;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.sap.acad.calculator.Calculator;
import org.apache.catalina.startup.Tomcat;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import javax.ws.rs.core.MediaType;

public class IntegrationTest {
    private Tomcat tomcat;
    private Calculator calculator;
    private String host = "http://localhost:8085";
    @Before
    public void setUp() throws Exception {
        this.calculator = new Calculator();
        tomcat = new Tomcat();
        tomcat.setPort(8085);
        tomcat.getConnector();
        tomcat.setBaseDir("temp");
        String contextPath = "/RestCalculator";
        String warFilePath = new File("").getAbsolutePath()+ "\\target\\WebCalculator.war";
        tomcat.getHost().setAppBase(".");
        tomcat.addWebapp(contextPath, warFilePath);
        tomcat.init();
        tomcat.start();
        Assertions.assertTrue(tomcat.getServer().getState().isAvailable());
    }

    @After
    public void tearDown() throws Exception {
        tomcat.stop();
        Assertions.assertFalse(tomcat.getServer().getState().isAvailable());
        tomcat.destroy();

    }
    private boolean isValidJSON(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            return false;
        }
        return true;
    }
    @Test
    public void verifyRESTServiceCorrectness() throws IOException, InterruptedException {
        String expression="5-3";
        String url = host+"/RestCalculator/calculator/expressions?expression="+expression;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());
        String json = response.body();
        Assertions.assertTrue(isValidJSON(json), "Does not return valid JSON");
        Double expectedAnswerToExpression = calculator.calculate(expression);
        JSONObject jsonObj = new JSONObject(json);
        Assertions.assertEquals(expectedAnswerToExpression, jsonObj.getDouble("answer"));
        Assertions.assertEquals(200, response.statusCode());
        Assertions.assertEquals(MediaType.APPLICATION_JSON,response.headers().firstValue("content-type").get());
    }
}
