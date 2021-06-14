package com.superestos;

import java.util.List;

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

    static class Var extends Statement {
        Var(Token name, Expression initalizer) {
            super(initalizer);
            this.name = name;
        }

        final Token name;
    }

    static class Block extends Statement {
        Block(List<Statement> statements) {
            super(null);
            this.statements = statements;
        }

        final List<Statement> statements;
    }
}
