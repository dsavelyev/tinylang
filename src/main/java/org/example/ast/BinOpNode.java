package org.example.ast;

import org.example.interp.Value;

public record BinOpNode(
        ExprNode lhs,
        BinOp binOp,
        ExprNode rhs
) implements ExprNode {
    @Override
    public Value visit(ExprVisitor visitor) {
        return visitor.visitBinOp(this);
    }
}
