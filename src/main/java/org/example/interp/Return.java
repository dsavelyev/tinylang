package org.example.interp;

public class Return extends RuntimeException {
    public Value value;

    public Return(Value value) { this.value = value; }
}
