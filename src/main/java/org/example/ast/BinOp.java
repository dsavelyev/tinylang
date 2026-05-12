package org.example.ast;

public enum BinOp {
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

    public static BinOp fromString(String token) {
        return switch (token) {
            case "==" -> BinOp.EQ;
            case "!=" -> BinOp.NE;
            case "<" -> BinOp.LT;
            case "<=" -> BinOp.LE;
            case ">" -> BinOp.GT;
            case ">=" -> BinOp.GE;
            case "+" -> BinOp.ADD;
            case "-" -> BinOp.SUB;
            case "*" -> BinOp.MUL;
            case "/" -> BinOp.DIV;
            default -> throw new AssertionError();
        };
    }
}
