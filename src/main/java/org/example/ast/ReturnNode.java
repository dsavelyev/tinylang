package org.example.ast;

public record ReturnNode(ExprNode expr) implements StmtNode {
    public void visit(StmtVisitor visitor) {
        visitor.visitReturn(this);
    }
}
