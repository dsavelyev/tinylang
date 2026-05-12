package org.example.ast;

public interface StmtNode {
    void visit(StmtVisitor visitor);
}
