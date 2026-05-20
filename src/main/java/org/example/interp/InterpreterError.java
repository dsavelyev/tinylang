package org.example.interp;

public class InterpreterError extends RuntimeException {
    public enum Kind {
        UNBOUND_VARIABLE,
        TYPE_MISMATCH,
        DIVISION_BY_ZERO,
        ARGUMENT_COUNT_MISMATCH,
        STACK_OVERFLOW
    }

    private final Kind kind;

    public InterpreterError(Kind kind, String message) {
        super(message);
        this.kind = kind;
    }

    public Kind kind() {
        return kind;
    }
}
