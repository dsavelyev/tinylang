package org.example.interp;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.io.IOException;
import java.io.Reader;
import org.example.GrammarLexer;
import org.example.GrammarParser;
import org.example.ast.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Interpreter implements ExprNode.Visitor, StmtNode.Visitor {
    ArrayList<HashMap<String, Slot>> stack = new ArrayList<>();

    @Override
    public void visitAssign(StmtNode.Assignment node) {
        setVariable(node.name(), node.expr().visit(this));
    }

    private void setVariable(String name, Value value) {
        setVariable(name, value, stack.getLast());
    }

    private void setVariable(String name, Value value, HashMap<String, Slot> scope) {
        var slot = scope.get(name);
        if (slot == null) {
            throw new AssertionError("no variable slot");
        }
        slot.value = value;
    }

    private Value getVariable(String name) {
        // slot present with null value = declared in this scope but not yet assigned
        // slot absent (map returns null) = not declared in this scope
        // so that as-yet-unassigned local variables still shadow globals

        for (var scope : stack.reversed()) {
            var slot = scope.get(name);
            if (slot != null) {
                if (slot.value == null)
                    throw new InterpreterError(InterpreterError.Kind.UNBOUND_VARIABLE,
                            String.format("variable %s not yet assigned", name));
                return slot.value;
            }
        }

        throw new InterpreterError(InterpreterError.Kind.UNBOUND_VARIABLE, String.format("undefined variable %s", name));
    }

    @Override
    public Value visitLogicalOp(ExprNode.LogicalOp node) {
        boolean left = node.lhs().visit(this).toBoolOrThrow();
        return switch (node.type()) {
            case AND -> new BoolValue(left && node.rhs().visit(this).toBoolOrThrow());
            case OR  -> new BoolValue(left || node.rhs().visit(this).toBoolOrThrow());
        };
    }

    @Override
    public Value visitUnaryOp(ExprNode.UnaryOp node) {
        return switch (node.type()) {
            case NEG -> new IntValue(-node.operand().visit(this).toIntOrThrow());
            case NOT -> new BoolValue(!node.operand().visit(this).toBoolOrThrow());
        };
    }

    @Override
    public Value visitBinOp(ExprNode.BinOp node) {
        int left = node.lhs().visit(this).toIntOrThrow();
        int right = node.rhs().visit(this).toIntOrThrow();

        try {
            return switch (node.type()) {
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
            throw new InterpreterError(InterpreterError.Kind.DIVISION_BY_ZERO, e.getMessage());
        }
    }

    private void visitStmts(ArrayList<StmtNode> nodes) {
        for (var node : nodes) {
            node.visit(this);
        }
    }

    @Override
    public void visitWhile(StmtNode.While node) {
        while (node.cond().visit(this).toBoolOrThrow()) {
            visitStmts(node.body().stmts());
        }
    }

    @Override
    public void visitIf(StmtNode.If node) {
        boolean cond = node.expr().visit(this).toBoolOrThrow();
        if (cond) {
            node.then().visit(this);
        } else {
            node.else_().visit(this);
        }
    }

    @Override
    public Value visitLiteral(ExprNode.Literal node) {
        return node.value();
    }

    @Override
    public void visitReturn(StmtNode.Return node) {
        // will be caught in the corresponding visitFuncCall
        throw new Return(node.expr().visit(this));
    }

    @Override
    public Value visitVarRef(ExprNode.VarRef node) {
        return getVariable(node.name());
    }

    private static HashMap<String, Slot> newScope(HashSet<String> locals) {
        var scope = new HashMap<String, Slot>();
        for (var name : locals) {
            scope.put(name, new Slot());
        }
        return scope;
    }

    @Override
    public Value visitFuncCall(ExprNode.FuncCall node) {
        var value = node.func().visit(this);
        StmtNode.FuncDecl func;
        try {
            func = ((FunctionValue) value).body();
        } catch(ClassCastException e) {
            throw new InterpreterError(InterpreterError.Kind.TYPE_MISMATCH, "attempted call of non-function");
        }

        var args = node.args().iterator();
        var params = func.params().iterator();
        var scope = newScope(func.locals());
        while (params.hasNext() && args.hasNext()) {
            setVariable(params.next(), args.next().visit(this), scope);
        }
        if (params.hasNext() || args.hasNext()) {
            throw new InterpreterError(InterpreterError.Kind.ARGUMENT_COUNT_MISMATCH, "argument count mismatch");
        }
        stack.add(scope);

        Value ret = new IntValue(0);
        try {
            visitStmts(func.body().stmts());
        } catch (Return r) {
            ret = r.value;
        }

        stack.removeLast();
        return ret;
    }

    @Override
    public void visitProgram(StmtNode.Program node) {
        var scope = newScope(node.locals());
        stack.add(scope);

        visitStmts(node.body().stmts());
    }

    @Override
    public void visitFuncDecl(StmtNode.FuncDecl node) {
        setVariable(node.name(), new FunctionValue(node));
    }

    @Override
    public void visitCompoundStmt(StmtNode.CompoundStmt node) {
        visitStmts(node.stmts());
    }

    public Map<String, Value> getAllVariables() {
        var result = new HashMap<String, Value>();
        for (var entry : stack.getLast().entrySet()) {
            if (entry.getValue().value != null) {
                result.put(entry.getKey(), entry.getValue().value);
            }
        }
        return result;
    }

    public void run(StmtNode.Program program) {
        try {
            visitProgram(program);
        } catch (StackOverflowError e) {
            throw new InterpreterError(InterpreterError.Kind.STACK_OVERFLOW, "stack overflow");
        }
    }

    public void run(String source) {
        var input = CharStreams.fromString(source);
        runFromCharStream(input);
    }

    public void run(Reader reader) throws IOException {
        var input = CharStreams.fromReader(reader);
        runFromCharStream(input);
    }

    private void runFromCharStream(org.antlr.v4.runtime.CharStream input) {
        var lexer = new GrammarLexer(input);
        var tokens = new CommonTokenStream(lexer);
        var parser = new GrammarParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);
        var program = (StmtNode.Program) new ASTVisitor().visit(parser.program());
        run(program);
    }
}
