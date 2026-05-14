package org.example;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.example.ast.ASTVisitor;
import org.example.ast.ProgramNode;
import org.example.ast.ThrowingErrorListener;
import org.example.interp.FunctionValue;
import org.example.interp.Interpreter;
import org.example.interp.InterpreterError;

import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
        CharStream input = null;
        try {
            input = CharStreams.fromReader(new InputStreamReader(System.in));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        var lexer = new GrammarLexer(input);

        var tokens = new CommonTokenStream(lexer);
        var parser = new GrammarParser(tokens);

        parser.removeErrorListeners();
        parser.addErrorListener(ThrowingErrorListener.INSTANCE);

        ProgramNode ast = null;

        try {
            var parseTree = parser.program();

            var visitor = new ASTVisitor();
            ast = (ProgramNode) visitor.visit(parseTree);

//            System.out.println("Parsed:\n" + ast.toString());
        } catch (ParseCancellationException e) {
            System.err.println("Compilation failed: " + e.getMessage());
            return;
        }

        var interp = new Interpreter();

        try {
            interp.visitProgram(ast);
        } catch (InterpreterError e) {
            System.err.println("Runtime error: " + e.getMessage());
            return;
        }

        for (var it : interp.getAllVariables().entrySet()) {
            if (!(it.getValue() instanceof FunctionValue)) {
                System.out.format("%s: %s\n", it.getKey(), it.getValue());
            }
        }
    }
}
