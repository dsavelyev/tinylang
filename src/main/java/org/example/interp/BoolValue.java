package org.example.interp;

public record BoolValue(boolean value) implements Value {
    @Override
    public String toString() { return String.valueOf(value); }

    public int toIntOrThrow() {
        return value ? 1 : 0;
    }

    public boolean toBoolOrThrow() {
        return value;
    }
}
