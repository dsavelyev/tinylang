package org.example.ast;

import java.util.ArrayList;

public record WhileNode(ExprNode cond, ArrayList<StmtNode> body) implements StmtNode {
    @Override
    public void visit(StmtVisitor visitor) {
        visitor.visitWhile(this);
    }
}
