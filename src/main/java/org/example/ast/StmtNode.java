package org.example.ast;

import java.util.ArrayList;
import java.util.HashSet;

public interface StmtNode {
    void visit(Visitor visitor);

    interface Visitor {
        void visitAssign(Assignment node);
        void visitReturn(Return node);
        void visitIf(If node);
        void visitWhile(While node);
        void visitFuncDecl(FuncDecl node);
        void visitCompoundStmt(CompoundStmt node);
        void visitProgram(Program node);
    }

    record Assignment(String name, ExprNode expr) implements StmtNode {
        public void visit(Visitor visitor) {
            visitor.visitAssign(this);
        }
    }

    record CompoundStmt(ArrayList<StmtNode> stmts) implements StmtNode {
        @Override
        public void visit(Visitor visitor) {
            visitor.visitCompoundStmt(this);
        }
    }

    record If(ExprNode expr, StmtNode then, StmtNode else_) implements StmtNode {
        public void visit(Visitor visitor) {
            visitor.visitIf(this);
        }
    }

    record Program(
            HashSet<String> locals,
            CompoundStmt body
    ) implements StmtNode {
        @Override
        public void visit(Visitor visitor) {
            visitor.visitProgram(this);
        }
    }

    record Return(ExprNode expr) implements StmtNode {
        public void visit(Visitor visitor) {
            visitor.visitReturn(this);
        }
    }

    record While(ExprNode cond, CompoundStmt body) implements StmtNode {
        @Override
        public void visit(Visitor visitor) {
            visitor.visitWhile(this);
        }
    }

    record FuncDecl(
            String name,
            ArrayList<String> params,
            HashSet<String> locals,
            CompoundStmt body
    ) implements StmtNode {
        @Override
        public void visit(Visitor visitor) {
            visitor.visitFuncDecl(this);
        }
    }
}
