package com.superestos;

import java.util.List;

abstract class Statement {

    static class Print extends Statement {
        Print(Expression expression) {
            this.expression = expression;
        }
        final Expression expression;
    }

    static class Expr extends Statement {
        Expr(Expression expression) {
            this.expression = expression;
        }

        final Expression expression;
    }

    static class Var extends Statement {
        Var(Token name, Expression initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        final Token name;
        final Expression initializer;
    }

    static class Block extends Statement {
        Block(List<Statement> statements) {
            this.statements = statements;
        }

        final List<Statement> statements;
    }

    static class If extends Statement {
        If(Expression condition, Statement thenBranch, Statement elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        final Expression condition;
        final Statement thenBranch;
        final Statement elseBranch;
    }

    static class While extends Statement {
        While(Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        final Expression condition;
        final Statement body;
    }

    static class Function extends Statement {
        Function(Token name, List<Token> parameters, List<Statement> body) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }

        final Token name;
        final List<Token> parameters;
        final List<Statement> body;
    }
}
