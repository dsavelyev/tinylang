package org.example.ast;

import java.util.ArrayList;

public record IfNode(ExprNode expr, StmtNode then, StmtNode else_) implements StmtNode {
    public void visit(StmtVisitor visitor) {
        visitor.visitIf(this);
    }
}
