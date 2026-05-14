package org.example.interp;

/// Represents a variable that exists in the corresponding scope
/// but has not been assigned yet
public final class SentinelValue extends Value {
    public static SentinelValue INSTANCE = new SentinelValue();

    private SentinelValue() {
    }

    @Override
    public String toString() {
        return "<unbound>";
    }
}
