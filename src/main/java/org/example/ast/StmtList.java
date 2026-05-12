package org.example.ast;

import java.util.ArrayList;

// Helper class for downcasting to in ASTVisitor
public record StmtList(ArrayList<StmtNode> list) {
}
