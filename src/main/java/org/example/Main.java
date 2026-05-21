package org.example;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.example.interp.FunctionValue;
import org.example.interp.Interpreter;
import org.example.interp.InterpreterError;

import java.io.IOException;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) {
        var interp = new Interpreter();
        try {
            interp.run(new InputStreamReader(System.in));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseCancellationException e) {
            System.err.println("Compilation failed: " + e.getMessage());
            return;
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
