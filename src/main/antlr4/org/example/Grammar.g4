grammar Grammar;

// Lexer Rules
IDENT  : [a-zA-Z_][a-zA-Z0-9_]* ;
INT : [0-9]+ ;
WS  : [ \t]+ -> skip ;

// Parser Rules
program: stmtBlock EOF;

stmtBlock: stmtGroup ('\n'+ stmtGroup)*;

statement: IDENT '=' expr       #AssignStmt
         | 'return' expr     #ReturnStmt
         | 'if' expr 'then' statement 'else' statement   # IfStmt
         | 'while' expr 'do' stmtGroup                   # WhileStmt
         | 'fun' IDENT '(' paramlist ')' compoundStmt       # FuncDecl
         | compoundStmt                                  # CurlyBlock
         ;

compoundStmt : '{' stmtBlock '}';
stmtGroup : (statement (',' statement)*)?;

paramlist: (IDENT (',' IDENT)*)?;
exprlist: (expr (',' expr)*)?;

expr: expr '(' exprlist ')'                         # CallExpr
    | expr op=('*'|'/') expr                      # MulExpr
    | expr op=('+'|'-') expr                      # AddExpr
    | expr op=('=='|'!='|'<'|'>'|'<='|'>=') expr  # CmpExpr
    | INT     #IntExpr
    | value=('true'|'false')  #BoolExpr
    | IDENT      #IdExpr
    | '(' expr ')'  #ParensExpr
    ;