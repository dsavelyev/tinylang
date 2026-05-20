package org.example.ast;

public record WhileNode(ExprNode cond, CompoundStmtNode body) implements StmtNode {
    @Override
    public void visit(Visitor visitor) {
        visitor.visitWhile(this);
    }
}
