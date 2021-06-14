package com.superestos;

abstract class Statement {

    final Expression expression;

    Statement(Expression expression) {
        this.expression = expression;
    }

    static class Print extends Statement {
        Print(Expression expression) {
            super(expression);
        }
    }

    static class Expr extends Statement {
        Expr(Expression expression) {
            super(expression);
        }
    }
}
