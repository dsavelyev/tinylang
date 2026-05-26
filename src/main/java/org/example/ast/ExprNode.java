package org.example.ast;

import org.example.interp.Value;

import java.util.ArrayList;

public interface ExprNode {
    Value visit(Visitor visitor);

    interface Visitor {
        Value visitBinOp(BinOp node);
        Value visitUnaryOp(UnaryOp node);
        Value visitLogicalOp(LogicalOp node);
        Value visitLiteral(Literal node);
        Value visitVarRef(VarRef node);
        Value visitFuncCall(FuncCall node);
    }

    record FuncCall(
            ExprNode func,
            ArrayList<ExprNode> args
    ) implements ExprNode {
        @Override
        public Value visit(Visitor visitor) {
            return visitor.visitFuncCall(this);
        }
    }

    record BinOp(
            ExprNode lhs,
            Type type,
            ExprNode rhs
    ) implements ExprNode {
        @Override
        public Value visit(Visitor visitor) {
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

    record Literal(Value value) implements ExprNode {
        public Value visit(Visitor visitor) {
            return visitor.visitLiteral(this);
        }
    }

    record LogicalOp(Type type, ExprNode lhs, ExprNode rhs) implements ExprNode {
        @Override
        public Value visit(Visitor visitor) {
            return visitor.visitLogicalOp(this);
        }

        public enum Type {
            AND,
            OR
        }
    }

    record UnaryOp(Type type, ExprNode operand) implements ExprNode {
        @Override
        public Value visit(Visitor visitor) {
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

    record VarRef(String name) implements ExprNode {
        public Value visit(Visitor visitor) {
            return visitor.visitVarRef(this);
        }
    }
}
