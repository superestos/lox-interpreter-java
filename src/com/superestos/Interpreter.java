package com.superestos;

import java.util.ArrayList;
import java.util.List;

public class Interpreter {

    final Environment globals = new Environment();
    private Environment environment = globals;

    void interpret(List<Statement> statements) {
        try {
            for (Statement statement: statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            Main.runtimeError(error);
        }
    }

    public void visitPrintStatement(Statement.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
    }

    public void visitBlockStatement(Statement.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
    }

    public void visitExprStatement(Statement.Expr stmt) {
        evaluate(stmt.expression);
    }

    public void visitVarStatement(Statement.Var stmt) {
        Object value = null;
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name, value);
    }

    public void visitIfStatement(Statement.If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch);
        } else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch);
        }
    }

    public void visitWhileStatement(Statement.While stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body);
        }
    }

    public void visitFunctionStatement(Statement.Function stmt) {
        Function function = new Function(stmt);
        environment.define(stmt.name, function);
    }

    public void visitReturnStatement(Statement.Return stmt) {
        Object value = null;
        if (stmt.value != null) {
            value = evaluate(stmt.value);
        }

        throw new Return(value);
    }

    public Object visitLiteralExpr(Expression.Literal expr) {
        return expr.value;
    }

    public Object visitGroupingExpr(Expression.Grouping expr) {
        return evaluate(expr.expression);
    }

    public Object visitUnaryExpr(Expression.Unary expr) {
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case BANG:
                return !isTruthy(right);
            case PLUS:
                return right;
            case MINUS:
                checkNumberOperand(expr.operator, right);
                return -(double)right;
        }

        return null;
    }

    public Object visitBinaryExpr(Expression.Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double)left + (double)right;
                }
                if (left instanceof String && right instanceof String) {
                    return (String)left + (String)right;
                }
                throw new RuntimeError(expr.operator,
                        "Operands must be two numbers or two strings.");

            case MINUS:
                checkNumberOperand(expr.operator, left, right);
                return (double)left - (double)right;
            case SLASH:
                checkNumberOperand(expr.operator, left, right);
                return (double)left / (double)right;
            case STAR:
                checkNumberOperand(expr.operator, left, right);
                return (double)left * (double)right;
            case GREATER:
                checkNumberOperand(expr.operator, left, right);
                return (double)left > (double)right;
            case GREATER_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double)left >= (double)right;
            case LESS:
                checkNumberOperand(expr.operator, left, right);
                return (double)left < (double)right;
            case LESS_EQUAL:
                checkNumberOperand(expr.operator, left, right);
                return (double)left <= (double)right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        return null;
    }

    public Object visitVariableExpr(Expression.Variable expr) {
        return environment.get(expr.name);
    }

    public Object visitAssignExpr(Expression.Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    public Object visitLogicalExpr(Expression.Logical expr) {
        Object left = evaluate(expr.left);
        if (expr.operator.type == TokenType.OR) {
            if (isTruthy(left)) {
                return left;
            }
        } else {
            if (!isTruthy(left)) {
                return left;
            }
        }

        return evaluate(expr.right);
    }

    public Object visitCallExpr(Expression.Call expr) {
        Object callee = evaluate(expr.callee);
        List<Object> arguments = new ArrayList<>();
        for (Expression argument: expr.arguments) {
            arguments.add(evaluate(argument));
        }

        if (!(callee instanceof Callable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        Callable function = (Callable)callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, "Expected " + function.arity() +
                    " arguments but got " + arguments.size() + ".");
        }

        return function.call(this, arguments);
    }

    private Object evaluate(Expression expr) {
        if (expr instanceof Expression.Binary) {
            return visitBinaryExpr((Expression.Binary) expr);
        }
        if (expr instanceof Expression.Unary) {
            return visitUnaryExpr((Expression.Unary) expr);
        }
        if (expr instanceof Expression.Grouping) {
            return visitGroupingExpr((Expression.Grouping) expr);
        }
        if (expr instanceof Expression.Literal) {
            return visitLiteralExpr((Expression.Literal) expr);
        }
        if (expr instanceof Expression.Variable) {
            return visitVariableExpr((Expression.Variable) expr);
        }
        if (expr instanceof Expression.Assign) {
            return visitAssignExpr((Expression.Assign) expr);
        }
        if (expr instanceof Expression.Logical) {
            return visitLogicalExpr((Expression.Logical) expr);
        }
        if (expr instanceof Expression.Call) {
            return visitCallExpr((Expression.Call) expr);
        }
        return null;
    }

    private void execute(Statement stmt) {
        if (stmt instanceof Statement.Print) {
            visitPrintStatement((Statement.Print) stmt);
        }
        if (stmt instanceof Statement.Expr) {
            visitExprStatement((Statement.Expr) stmt);
        }
        if (stmt instanceof Statement.Var) {
            visitVarStatement((Statement.Var) stmt);
        }
        if (stmt instanceof Statement.Block) {
            visitBlockStatement((Statement.Block) stmt);
        }
        if (stmt instanceof Statement.If) {
            visitIfStatement((Statement.If) stmt);
        }
        if (stmt instanceof Statement.While) {
            visitWhileStatement((Statement.While) stmt);
        }
        if (stmt instanceof Statement.Function) {
            visitFunctionStatement((Statement.Function) stmt);
        }
        if (stmt instanceof Statement.Return) {
            visitReturnStatement((Statement.Return) stmt);
        }
    }

    public void executeBlock(List<Statement> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Statement statement: statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }

        return a.equals(b);
    }

    private boolean isTruthy(Object object) {
        if (object == null) {
            return false;
        }
        if (object instanceof Boolean) {
            return (boolean)object;
        }
        return true;
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperand(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;
        throw new RuntimeError(operator, "Operand must be a number.");
    }

    static class RuntimeError extends RuntimeException {
        final Token token;

        RuntimeError(Token token, String message) {
            super(message);
            this.token = token;
        }
    }

    static class Return extends RuntimeException {
        final Object value;

        Return(Object value) {
            super(null, null, false, false);
            this.value = value;
        }
    }
}
