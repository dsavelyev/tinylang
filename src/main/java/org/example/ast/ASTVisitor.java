package org.example.ast;

import org.example.interp.BoolValue;
import org.example.GrammarBaseVisitor;
import org.example.GrammarParser;
import org.example.interp.IntValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ASTVisitor extends GrammarBaseVisitor<Object> {
    public ArrayList<HashSet<String>> stack = new ArrayList<>();

    @Override
    public Object visitAssignStmt(GrammarParser.AssignStmtContext ctx) {
        stack.getLast().add(ctx.IDENT().getText());
        return new AssignmentNode(ctx.IDENT().getText(), (ExprNode) visit(ctx.expr()));
    }

    @Override
    public Object visitAddExpr(GrammarParser.AddExprContext ctx) {
        return new BinOpNode((ExprNode) visit(ctx.expr(0)),
                BinOp.fromString(ctx.op.getText()),
                (ExprNode) visit(ctx.expr(1)));
    }

    @Override
    public Object visitMulExpr(GrammarParser.MulExprContext ctx) {
        return new BinOpNode((ExprNode) visit(ctx.expr(0)),
                BinOp.fromString(ctx.op.getText()),
                (ExprNode) visit(ctx.expr(1)));
    }

    @Override
    public Object visitCmpExpr(GrammarParser.CmpExprContext ctx) {
        return new BinOpNode((ExprNode) visit(ctx.expr(0)),
                BinOp.fromString(ctx.op.getText()),
                (ExprNode) visit(ctx.expr(1)));
    }

    @Override
    public Object visitCallExpr(GrammarParser.CallExprContext ctx) {
        var el = ctx.exprlist();
        var args = new ArrayList<ExprNode>();
        for (var arg : el.expr())
            args.add((ExprNode)visit(arg));
        return new FuncCallNode(ctx.IDENT().getText(), args);
    }

    @Override
    public Object visitIntExpr(GrammarParser.IntExprContext ctx) {
        return new LiteralNode(new IntValue(Integer.parseInt(ctx.INT().getText())));
    }

    @Override
    public Object visitBoolExpr(GrammarParser.BoolExprContext ctx) {
        boolean b = switch (ctx.getText()) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new AssertionError("unexpected bool token");
        };

        return new LiteralNode(new BoolValue(b));
    }

    @Override
    public Object visitIdExpr(GrammarParser.IdExprContext ctx) {
        return new VarNode(ctx.getText());
    }

    @Override
    public Object visitParensExpr(GrammarParser.ParensExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Object visitReturnStmt(GrammarParser.ReturnStmtContext ctx) {
        return new ReturnNode((ExprNode)visit(ctx.expr()));
    }

    @Override
    public Object visitStmtGroup(GrammarParser.StmtGroupContext ctx) {
        return visitStmts(ctx.statement());
    }

    @Override
    public Object visitCompoundStmt(GrammarParser.CompoundStmtContext ctx) {
        return visit(ctx.stmtBlock());
    }

    @Override
    public Object visitStmtBlock(GrammarParser.StmtBlockContext ctx) {
        var stmts = new ArrayList<StmtNode>();
        for (var group : ctx.stmtGroup()) {
            var newStmts = ((StmtList)visitStmtGroup(group)).list();
            stmts.addAll(newStmts);
        }
        return new StmtList(stmts);
    }

    private Object visitStmts(List<GrammarParser.StatementContext> statement) {
        var stmts = new ArrayList<StmtNode>();
        for (var stmt : statement)
            stmts.add((StmtNode)visit(stmt));
        return new StmtList(stmts);
    }

    @Override
    public Object visitIfStmt(GrammarParser.IfStmtContext ctx) {
        return new IfNode((ExprNode)visit(ctx.expr()),
                (StmtNode)visit(ctx.statement(0)),
                (StmtNode)visit(ctx.statement(1)));
    }

    @Override
    public Object visitWhileStmt(GrammarParser.WhileStmtContext ctx) {
        return new WhileNode((ExprNode)visit(ctx.expr()),
                ((StmtList)visit(ctx.stmtGroup())).list());
    }

    @Override
    public Object visitFuncDecl(GrammarParser.FuncDeclContext ctx) {
        stack.getLast().add(ctx.IDENT().getText());

        var params = new ArrayList<String>();
        var scope = new HashSet<String>();
        for (var param : ctx.paramlist().IDENT()) {
            params.add(param.getText());
            scope.add(param.getText());
        }

        stack.add(scope);

        var stmts = ((StmtList)visit(ctx.compoundStmt())).list();

        stack.removeLast();

        return new FuncDeclNode(ctx.IDENT().getText(), params, scope,
                stmts);
    }

    @Override
    public Object visitProgram(GrammarParser.ProgramContext ctx) {
        var scope = new HashSet<String>();
        stack.add(scope);

        var stmts = ((StmtList)visit(ctx.stmtBlock())).list();

        stack.removeLast();
        return new ProgramNode(scope, stmts);
    }
}
