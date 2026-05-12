package org.example.ast;

import org.example.interp.Value;

public interface ExprVisitor {
    Value visitBinOp(BinOpNode node);
    Value visitLiteral(LiteralNode node);
    Value visitVar(VarNode node);
    Value visitFuncCall(FuncCallNode node);
}
