package org.example.ast;

import org.example.interp.Value;

public record UnaryOpNode(Type type, ExprNode operand) implements ExprNode {
    @Override
    public Value visit(ExprVisitor visitor) {
        return visitor.visitUnaryOp(this);
    }

    public enum Type {
        NEG,
        NOT;

        public static Type fromString(String token) {
            return switch (token) {
                case "-" -> NEG;
                case "!" -> NOT;
                default -> throw new AssertionError("unknown unary op: " + token);
            };
        }
    }
}
