package org.example.interp;

public interface Value {
    default boolean toBoolOrThrow() {
        throw new InterpreterError("type mismatch, expected boolean");
    }

    default int toIntOrThrow() {
        throw new InterpreterError("type mismatch, expected int");
    }
}
