package org.example.ast;

import org.example.interp.Value;

public interface ExprNode {
    Value visit(ExprVisitor visitor);
}
