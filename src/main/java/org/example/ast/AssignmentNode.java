package org.example.ast;

public record AssignmentNode(String name, ExprNode expr) implements StmtNode {
    public void visit(Visitor visitor) {
        visitor.visitAssign(this);
    }
}
