package org.example.interp;

import org.example.ast.FuncDeclNode;

public record FunctionValue(FuncDeclNode body) implements Value {
}
