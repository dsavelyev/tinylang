package org.example.ast;

public record ReturnNode(ExprNode expr) implements StmtNode {
    public void visit(Visitor visitor) {
        visitor.visitReturn(this);
    }
}
