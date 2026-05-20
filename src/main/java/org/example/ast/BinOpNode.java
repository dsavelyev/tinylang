package org.example.ast;

import org.example.interp.Value;

public record BinOpNode(
        ExprNode lhs,
        Type type,
        ExprNode rhs
) implements ExprNode {
    @Override
    public Value visit(ExprVisitor visitor) {
        return visitor.visitBinOp(this);
    }

    public enum Type {
        ADD,
        SUB,
        MUL,
        DIV,
        EQ,
        NE,
        LT,
        LE,
        GT,
        GE;

        public static Type fromString(String token) {
            return switch (token) {
                case "==" -> Type.EQ;
                case "!=" -> Type.NE;
                case "<" -> Type.LT;
                case "<=" -> Type.LE;
                case ">" -> Type.GT;
                case ">=" -> Type.GE;
                case "+" -> Type.ADD;
                case "-" -> Type.SUB;
                case "*" -> Type.MUL;
                case "/" -> Type.DIV;
                default -> throw new AssertionError();
            };
        }
    }
}
