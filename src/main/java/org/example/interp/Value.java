package org.example.interp;

public abstract class Value {
    public boolean toBoolOrThrow() {
        throw new InterpreterError("type mismatch, expected boolean");
    }

    public int toIntOrThrow() {
        throw new InterpreterError("type mismatch, expected int");
    }
}
