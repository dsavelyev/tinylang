package org.example.interp;

import org.example.ast.FuncDeclNode;

import java.util.Objects;

public final class FunctionValue extends Value {
    private final FuncDeclNode body;

    public FunctionValue(FuncDeclNode body) {
        this.body = body;
    }

    public FuncDeclNode body() {
        return body;
    }

    @Override
    public String toString() {
        return "FunctionValue[" +
                "body=" + body + ']';
    }
}
