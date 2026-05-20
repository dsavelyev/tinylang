package org.example.ast;

import org.example.interp.Value;

public record UnaryOpNode(UnaryOp op, ExprNode operand) implements ExprNode {
    @Override
    public Value visit(ExprVisitor visitor) {
        return visitor.visitUnaryOp(this);
    }
}
