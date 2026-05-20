package org.example.ast;

import org.example.interp.Value;

public record VarNode(String name) implements ExprNode {
    public Value visit(Visitor visitor) {
        return visitor.visitVar(this);
    }
}
