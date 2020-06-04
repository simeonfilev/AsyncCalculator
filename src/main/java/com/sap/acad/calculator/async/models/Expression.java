package com.sap.acad.calculator.async.models;

import java.util.List;
import java.util.Stack;

public class Expression {
    private int id;
    private String expression;
    private Double answer;
    private boolean calculated;
    private String username;

    private static final List<Character> validSymbols = List.of('*', '/', '+', '-', '(', ')');
    private static final List<Character> mathOperators = List.of('*', '/', '+', '-');

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

    public Expression(String expression, String username) {
        this.expression = expression;
        this.username = username;
    }

    public Expression(int id, String expression, Double answer, boolean calculated) {
        this.id = id;
        this.expression = expression;
        this.answer = answer;
        this.calculated = calculated;
    }

    public Expression(int id, String expression, Double answer, boolean calculated, String username) {
        this.id = id;
        this.expression = expression;
        this.answer = answer;
        this.calculated = calculated;
        this.username = username;
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

    public String getUsername() {
        return username;
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
    private boolean parenthesesAreNotMatching(String expression)  {
        Stack<Character> parenthesesStack = new Stack<>();
        for (int index = 0; index < expression.length(); index++) {
            char currentChar = expression.charAt(index);
            if (currentChar == '(') {
                parenthesesStack.push(currentChar);
            }
            if (currentChar == ')') {
                if (parenthesesStack.isEmpty() || parenthesesStack.peek() != '(') {
                    return true;
                }
                parenthesesStack.pop();
            }
        }
        return !parenthesesStack.isEmpty();
    }

    private boolean isMathOperator(char c){
        return mathOperators.contains(c);
    }

    private boolean containsMultipleMathOperators(String expression){
        for(int index =0; index<expression.length()-1;index++){
            char currentChar = expression.charAt(index);
            char nextChar = expression.charAt(index+1);
            if(isMathOperator(currentChar) && currentChar == nextChar){
                return true;
            }
        }
        return false;
    }

    private boolean startsWithASignOtherThanMinus(String expression){
        return isMathOperator(expression.charAt(0)) && expression.charAt(0) != '-';
    }

    private boolean containsIllegalSymbols(String expression) {
        for (int index = 0; index < expression.length(); index++) {
            char currentChar = expression.charAt(index);
            if (!(Character.isDigit(currentChar) || validSymbols.contains(currentChar))) {
                return true;
            }
        }
        return false;
    }

    private boolean containsIllegalArguments(String expression) {
        return containsIllegalSymbols(expression) || parenthesesAreNotMatching(expression)
                || containsMultipleMathOperators(expression) || expression.equals("")
                || startsWithASignOtherThanMinus(expression);
    }

    public boolean isValidExpression(){
        return !containsIllegalArguments(this.getExpression());
    }
}
