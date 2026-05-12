package org.example.interp;

import org.example.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Interpreter implements ExprVisitor, StmtVisitor {
    ArrayList<HashMap<String, Value>> stack = new ArrayList<>();

    @Override
    public void visitAssign(AssignmentNode node) {
        setVariable(node.name(), node.expr().visit(this));
    }

    private void setVariable(String name, Value value) {
        setVariable(name, value, stack.getLast());
    }

    private void setVariable(String name, Value value, HashMap<String, Value> scope) {
        if (!scope.containsKey(name)) {
            throw new AssertionError("no variable slot");
        }
        scope.put(name, value);
    }

    private Value getVariable(String name) {
        // SentinelValue = variable assigned in this scope but not yet bound
        // null = variable never assigned in this scope
        // so that as-yet-unassigned local variables don't shadow globals

        for (var scope : stack.reversed()) {
            var value = scope.get(name);
            if (value != null) {
                if (value instanceof SentinelValue)
                    throw new InterpreterError(String.format("unbound variable %s", name));
                return value;
            }
        }

        throw new InterpreterError(String.format("undefined variable %s", name));
    }

    @Override
    public Value visitBinOp(BinOpNode node) {
        int left = node.lhs().visit(this).toIntOrThrow();
        int right = node.rhs().visit(this).toIntOrThrow();

        try {
            return switch (node.binOp()) {
                case ADD -> new IntValue(left + right);
                case SUB -> new IntValue(left - right);
                case MUL -> new IntValue(left * right);
                case DIV -> new IntValue(left / right);
                case EQ -> new BoolValue(left == right);
                case NE -> new BoolValue(left != right);
                case LT -> new BoolValue(left < right);
                case LE -> new BoolValue(left <= right);
                case GT -> new BoolValue(left > right);
                case GE -> new BoolValue(left >= right);
            };
        } catch (ArithmeticException e) {
            throw new InterpreterError(e.getMessage());
        }
    }

    private void visitBody(ArrayList<StmtNode> nodes) {
        for (var node : nodes) {
            node.visit(this);
        }
    }

    @Override
    public void visitWhile(WhileNode node) {
        while (node.cond().visit(this).toBoolOrThrow()) {
            visitBody(node.body());
        }
    }

    @Override
    public void visitIf(IfNode node) {
        boolean cond = node.expr().visit(this).toBoolOrThrow();
        if (cond) {
            node.then().visit(this);
        } else {
            node.else_().visit(this);
        }
    }

    @Override
    public Value visitLiteral(LiteralNode node) {
        return node.value();
    }

    @Override
    public void visitReturn(ReturnNode node) {
        throw new Return(node.expr().visit(this));
    }

    @Override
    public Value visitVar(VarNode node) {
        return getVariable(node.name());
    }

    private static HashMap<String, Value> newScope(HashSet<String> locals) {
        var scope = new HashMap<String, Value>();
        for (var name : locals) {
            // create empty slots for all locals assigned in this scope
            // (see getVariable)
            scope.put(name, SentinelValue.INSTANCE);
        }
        return scope;
    }

    @Override
    public Value visitFuncCall(FuncCallNode node) {
        var func = ((FunctionValue)getVariable(node.name())).body();

        var args = node.args().stream().map((argnode) -> argnode.visit(this))
                .iterator();
        var params = func.params().iterator();

        var scope = newScope(func.locals());
        while (params.hasNext()) {
            setVariable(params.next(), args.next(), scope);
        }
        stack.add(scope);

        Value ret = new IntValue(0);
        try {
            visitBody(func.body());
        } catch (Return r) {
            ret = r.value;
        }

        stack.removeLast();
        return ret;
    }

    @Override
    public void visitProgram(ProgramNode node) {
        var scope = newScope(node.locals());
        stack.add(scope);

        visitBody(node.body());
    }

    @Override
    public void visitFuncDecl(FuncDeclNode node) {
        setVariable(node.name(), new FunctionValue(node));
    }

    public HashMap<String, Value> getAllVariables() {
        return stack.getLast();
    }
}
