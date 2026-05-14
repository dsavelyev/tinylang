package org.example.interp;

import java.util.Objects;

public final class IntValue extends Value {
    private final int value;

    public IntValue(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public int toIntOrThrow() {
        return value;
    }

    public boolean toBoolOrThrow() {
        return value != 0;
    }

    public int value() {
        return value;
    }
}
