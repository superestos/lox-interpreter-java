package com.superestos;

import java.util.ArrayList;
import java.util.List;

import static com.superestos.TokenType.*;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Statement> parse() {
        List<Statement> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(statement());
        }

        return statements;
    }

    private Statement statement() {
        if (match(PRINT)) {
            return printStatement();
        }

        return expressionStatement();
    }

    private Statement printStatement() {
        Expression value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Statement.Print(value);
    }

    private Statement expressionStatement() {
        Expression expr = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Statement.Expr(expr);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) {
            return false;
        }

        return peek().type == type;
    }

    private boolean match(TokenType... types) {
        for (TokenType type: types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private Expression expression() {
        return equality();
    }

    private Expression equality() {
        Expression expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expression right = comparison();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression comparison() {
        Expression expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expression right = term();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression term() {
        Expression expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expression right = factor();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression factor() {
        Expression expr = unary();

        while (match(SLASH, STAR)) {
            Token operator = previous();
            Expression right = unary();
            expr = new Expression.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expression unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expression expr = unary();
            return new Expression.Unary(operator, expr);
        }

        return primary();
    }

    private Expression primary() {
        if (match(FALSE)) {
            return new Expression.Literal(false);
        }
        if (match(TRUE)) {
            return new Expression.Literal(true);
        }
        if (match(NIL)) {
            return new Expression.Literal(null);
        }

        if (match(NUMBER, STRING)) {
            return new Expression.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expression expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expr);
        }

        throw error(peek(), "Expect expression,");
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        Main.error(token, message);
        return new ParseError();
    }

    static class ParseError extends RuntimeException {}
}
