package org.example.ast;

import java.util.ArrayList;

public record CompoundStmtNode(ArrayList<StmtNode> stmts) implements StmtNode {
    @Override
    public void visit(Visitor visitor) {
        visitor.visitCompoundStmt(this);
    }
}
