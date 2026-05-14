package org.example.ast;

import org.example.interp.Value;

import java.util.ArrayList;

public record FuncCallNode(
        ExprNode func,
        ArrayList<ExprNode> args
) implements ExprNode {
    @Override
    public Value visit(ExprVisitor visitor) {
        return visitor.visitFuncCall(this);
    }
}
