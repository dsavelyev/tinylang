package org.example.interp;

/// Represents a variable that exists in the corresponding scope
/// but has not been assigned yet
public record SentinelValue() implements Value {
    public static SentinelValue INSTANCE = new SentinelValue();
}
