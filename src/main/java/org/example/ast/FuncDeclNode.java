package org.example.ast;

import java.util.ArrayList;
import java.util.HashSet;

public record FuncDeclNode(
        String name,
        ArrayList<String> params,
        HashSet<String> locals,
        ArrayList<StmtNode> body
) implements StmtNode {
    @Override
    public void visit(StmtVisitor visitor) {
        visitor.visitFuncDecl(this);
    }
}
