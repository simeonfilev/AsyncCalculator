package com.sap.acad.calculator.async.models;

public class Expression {
    private int id;
    private String expression;
    private Double answer;
    private boolean calculated;

    public Expression() {
    }

    public Expression(String expression) {
        this.expression = expression;
    }

    public Expression(int id, String expression, Double answer) {
        this.id = id;
        this.expression = expression;
        this.answer = answer;
    }

    public Expression(String expression, Double answer) {
        this.expression = expression;
        this.answer = answer;
    }

    public Expression(int id, String expression, Double answer, boolean calculated) {
        this.id = id;
        this.expression = expression;
        this.answer = answer;
        this.calculated = calculated;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Double getAnswer() {
        return answer;
    }

    public void setAnswer(Double answer) {
        this.answer = answer;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isCalculated() {
        return calculated;
    }

    public void setCalculated(boolean calculated) {
        this.calculated = calculated;
    }

    @Override
    public String toString() {
        return "Expression{" +
                "id=" + id +
                ", expression='" + expression + '\'' +
                ", answer=" + answer +
                ", calculated=" + calculated +
                '}';
    }
}
