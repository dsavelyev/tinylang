package org.example.ast;

public interface StmtVisitor {
    void visitAssign(AssignmentNode node);
    void visitReturn(ReturnNode node);
    void visitIf(IfNode node);
    void visitWhile(WhileNode node);
    void visitFuncDecl(FuncDeclNode node);
    void visitProgram(ProgramNode node);
}
