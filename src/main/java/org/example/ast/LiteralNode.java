package org.example.ast;

import org.example.interp.Value;

public record LiteralNode(Value value) implements ExprNode {
    public Value visit(Visitor visitor) {
        return visitor.visitLiteral(this);
    }
}
