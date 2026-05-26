package org.example.interp;

import org.example.ast.StmtNode;

public final class FunctionValue extends Value {
    private final StmtNode.FuncDecl body;

    public FunctionValue(StmtNode.FuncDecl body) {
        this.body = body;
    }

    public StmtNode.FuncDecl body() {
        return body;
    }

    @Override
    public String toString() {
        return "FunctionValue[" +
                "body=" + body + ']';
    }
}
