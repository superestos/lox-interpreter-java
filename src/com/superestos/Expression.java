package com.superestos;

import java.util.List;

abstract class Expression {

    static class Binary extends Expression {
        Binary(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        final Expression left;
        final Token operator;
        final Expression right;
    }

    static class Unary extends Expression {
        Unary(Token operator, Expression right) {
            this.operator = operator;
            this.right = right;
        }

        final Token operator;
        final Expression right;
    }

    static class Literal extends Expression {
        Literal(Object value) {
            this.value = value;
        }

        final Object value;
    }

    static class Grouping extends Expression {
        Grouping(Expression expression) {
            this.expression = expression;
        }

        final Expression expression;
    }

    static class Variable extends Expression {
        Variable(Token name) {
            this.name = name;
        }

        final Token name;
    }

    static class Assign extends Expression {
        Assign(Token name, Expression value) {
            this.name = name;
            this.value = value;
        }

        final Token name;
        final Expression value;
    }

    static class Logical extends Expression {
        Logical(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        final Expression left;
        final Token operator;
        final Expression right;
    }

    static class Call extends Expression {
        Call(Expression callee, List<Expression> arguments, Token paren) {
            this.callee = callee;
            this.arguments = arguments;
            this.paren = paren;
        }

        final Expression callee;
        final List<Expression> arguments;
        final Token paren;
    }
}
