package org.example.ast;

public record WhileNode(ExprNode cond, CompoundStmtNode body) implements StmtNode {
    @Override
    public void visit(StmtVisitor visitor) {
        visitor.visitWhile(this);
    }
}
