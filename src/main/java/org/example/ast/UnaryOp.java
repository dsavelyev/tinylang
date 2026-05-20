package org.example.ast;

public enum UnaryOp {
    NEG,
    NOT;

    public static UnaryOp fromString(String token) {
        return switch (token) {
            case "-" -> NEG;
            case "!" -> NOT;
            default -> throw new AssertionError("unknown unary op: " + token);
        };
    }
}
