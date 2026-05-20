package org.example.interp;

public abstract class Value {
    public boolean toBoolOrThrow() {
        throw new InterpreterError(InterpreterError.Kind.TYPE_MISMATCH, "type mismatch, expected boolean");
    }

    public int toIntOrThrow() {
        throw new InterpreterError(InterpreterError.Kind.TYPE_MISMATCH, "type mismatch, expected int");
    }
}
