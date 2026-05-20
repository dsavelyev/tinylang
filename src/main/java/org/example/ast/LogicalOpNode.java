package org.example.ast;

import org.example.interp.Value;

public record LogicalOpNode(Type type, ExprNode lhs, ExprNode rhs) implements ExprNode {
    @Override
    public Value visit(Visitor visitor) {
        return visitor.visitLogicalOp(this);
    }

    public enum Type {
        AND,
        OR
    }
}
