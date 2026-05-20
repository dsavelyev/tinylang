package org.example.ast;

import org.example.interp.Value;

public interface ExprNode {
    Value visit(Visitor visitor);

    interface Visitor {
        Value visitBinOp(BinOpNode node);
        Value visitUnaryOp(UnaryOpNode node);
        Value visitLogicalOp(LogicalOpNode node);
        Value visitLiteral(LiteralNode node);
        Value visitVar(VarNode node);
        Value visitFuncCall(FuncCallNode node);
    }
}
