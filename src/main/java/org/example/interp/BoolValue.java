package org.example.interp;

import java.util.Objects;

public final class BoolValue extends Value {
    private final boolean value;

    public BoolValue(boolean value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public int toIntOrThrow() {
        return value ? 1 : 0;
    }

    public boolean toBoolOrThrow() {
        return value;
    }

    public boolean value() {
        return value;
    }
}
