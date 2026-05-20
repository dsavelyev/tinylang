package org.example.interp;

/// Used for control flow: thrown when an interpreted function returns, caught in the function call visitor.
public class Return extends RuntimeException {
    public Value value;

    public Return(Value value) { this.value = value; }
}
