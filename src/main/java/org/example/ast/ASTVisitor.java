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
        return new StmtNode.Assignment(ctx.IDENT().getText(), (ExprNode) visit(ctx.expr()));
    }

    @Override
    public Object visitAndExpr(GrammarParser.AndExprContext ctx) {
        return new ExprNode.LogicalOp(ExprNode.LogicalOp.Type.AND, (ExprNode) visit(ctx.expr(0)), (ExprNode) visit(ctx.expr(1)));
    }

    @Override
    public Object visitOrExpr(GrammarParser.OrExprContext ctx) {
        return new ExprNode.LogicalOp(ExprNode.LogicalOp.Type.OR, (ExprNode) visit(ctx.expr(0)), (ExprNode) visit(ctx.expr(1)));
    }

    @Override
    public Object visitUnaryExpr(GrammarParser.UnaryExprContext ctx) {
        return new ExprNode.UnaryOp(ExprNode.UnaryOp.Type.fromString(ctx.op.getText()), (ExprNode) visit(ctx.expr()));
    }

    @Override
    public Object visitAddExpr(GrammarParser.AddExprContext ctx) {
        return new ExprNode.BinOp((ExprNode) visit(ctx.expr(0)),
                ExprNode.BinOp.Type.fromString(ctx.op.getText()),
                (ExprNode) visit(ctx.expr(1)));
    }

    @Override
    public Object visitMulExpr(GrammarParser.MulExprContext ctx) {
        return new ExprNode.BinOp((ExprNode) visit(ctx.expr(0)),
                ExprNode.BinOp.Type.fromString(ctx.op.getText()),
                (ExprNode) visit(ctx.expr(1)));
    }

    @Override
    public Object visitCmpExpr(GrammarParser.CmpExprContext ctx) {
        return new ExprNode.BinOp((ExprNode) visit(ctx.expr(0)),
                ExprNode.BinOp.Type.fromString(ctx.op.getText()),
                (ExprNode) visit(ctx.expr(1)));
    }

    @Override
    public Object visitCallExpr(GrammarParser.CallExprContext ctx) {
        var el = ctx.exprlist();
        var args = new ArrayList<ExprNode>();
        for (var arg : el.expr())
            args.add((ExprNode)visit(arg));
        return new ExprNode.FuncCall((ExprNode)visit(ctx.expr()), args);
    }

    @Override
    public Object visitIntExpr(GrammarParser.IntExprContext ctx) {
        return new ExprNode.Literal(new IntValue(Integer.parseInt(ctx.INT().getText())));
    }

    @Override
    public Object visitBoolExpr(GrammarParser.BoolExprContext ctx) {
        boolean b = switch (ctx.getText()) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new AssertionError("unexpected bool token");
        };

        return new ExprNode.Literal(new BoolValue(b));
    }

    @Override
    public Object visitIdExpr(GrammarParser.IdExprContext ctx) {
        return new ExprNode.VarRef(ctx.getText());
    }

    @Override
    public Object visitParensExpr(GrammarParser.ParensExprContext ctx) {
        return visit(ctx.expr());
    }

    @Override
    public Object visitReturnStmt(GrammarParser.ReturnStmtContext ctx) {
        return new StmtNode.Return((ExprNode)visit(ctx.expr()));
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
            var newStmts = ((StmtNode.CompoundStmt)visit(group)).stmts();
            stmts.addAll(newStmts);
        }
        return new StmtNode.CompoundStmt(stmts);
    }

    private StmtNode.CompoundStmt visitStmts(List<GrammarParser.StatementContext> statement) {
        var stmts = new ArrayList<StmtNode>();
        for (var stmt : statement) {
            var node = visit(stmt);
            stmts.add((StmtNode)node);
        }
        return new StmtNode.CompoundStmt(stmts);
    }

    @Override
    public Object visitIfStmt(GrammarParser.IfStmtContext ctx) {
        return new StmtNode.If((ExprNode)visit(ctx.expr()),
                (StmtNode)visit(ctx.statement(0)),
                (StmtNode)visit(ctx.statement(1)));
    }

    @Override
    public Object visitWhileStmt(GrammarParser.WhileStmtContext ctx) {
        return new StmtNode.While((ExprNode)visit(ctx.expr()),
                (StmtNode.CompoundStmt)visit(ctx.stmtGroup()));
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

        var stmts = (StmtNode.CompoundStmt)visit(ctx.compoundStmt());

        stack.removeLast();

        return new StmtNode.FuncDecl(ctx.IDENT().getText(), params, scope, stmts);
    }

    @Override
    public Object visitProgram(GrammarParser.ProgramContext ctx) {
        var scope = new HashSet<String>();
        stack.add(scope);

        var stmts = (StmtNode.CompoundStmt)visit(ctx.stmtBlock());

        stack.removeLast();
        return new StmtNode.Program(scope, stmts);
    }
}
