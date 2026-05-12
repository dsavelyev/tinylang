package org.example.interp;

public class InterpreterError extends RuntimeException {
    public InterpreterError(String message) {
        super(message);
    }
}
