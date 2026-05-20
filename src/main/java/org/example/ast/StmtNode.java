package org.example.ast;

public interface StmtNode {
    void visit(Visitor visitor);

    interface Visitor {
        void visitAssign(AssignmentNode node);
        void visitReturn(ReturnNode node);
        void visitIf(IfNode node);
        void visitWhile(WhileNode node);
        void visitFuncDecl(FuncDeclNode node);
        void visitCompoundStmt(CompoundStmtNode node);
        void visitProgram(Program node);
    }
}
