package org.example.interp;

public record IntValue(int value) implements Value {
    @Override
    public String toString() { return String.valueOf(value); }

    public int toIntOrThrow() {
        return value;
    }

    public boolean toBoolOrThrow() {
        return value != 0;
    }
}
