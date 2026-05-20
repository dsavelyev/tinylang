package org.example.interp;

public abstract class Value {
    public boolean toBoolOrThrow() {
        return true;
    }

    public int toIntOrThrow() {
        throw new InterpreterError(InterpreterError.Kind.TYPE_MISMATCH, "type mismatch, expected int");
    }
}
