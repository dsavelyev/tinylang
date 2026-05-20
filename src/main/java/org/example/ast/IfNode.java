package org.example.ast;

public record IfNode(ExprNode expr, StmtNode then, StmtNode else_) implements StmtNode {
    public void visit(Visitor visitor) {
        visitor.visitIf(this);
    }
}
