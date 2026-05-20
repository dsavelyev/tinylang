package org.example.ast;

import org.example.interp.Value;

public record LogicalOpNode(LogicalOp op, ExprNode lhs, ExprNode rhs) implements ExprNode {
    @Override
    public Value visit(ExprVisitor visitor) {
        return visitor.visitLogicalOp(this);
    }
}
