package org.example.ast;

import java.util.ArrayList;
import java.util.HashSet;

public record ProgramNode(
        HashSet<String> locals,
        CompoundStmtNode body
) implements StmtNode {
    @Override
    public void visit(StmtVisitor visitor) {
        visitor.visitProgram(this);
    }
}
