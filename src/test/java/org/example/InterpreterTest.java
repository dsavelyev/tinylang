package org.example;

import org.example.interp.BoolValue;
import org.example.interp.Interpreter;
import org.example.interp.InterpreterError;
import org.example.interp.IntValue;
import org.example.interp.Value;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    private static Map<String, Value> run(String source) {
        return Interpreter.run(source).getAllVariables();
    }

    private static int intVar(Map<String, Value> vars, String name) {
        return ((IntValue) vars.get(name)).value();
    }

    private static boolean boolVar(Map<String, Value> vars, String name) {
        return ((BoolValue) vars.get(name)).value();
    }

    // --- arithmetic ---

    @Test
    void literalAssignment() {
        var vars = run("x = 42");
        assertEquals(42, intVar(vars, "x"));
    }

    @Test
    void addAndMultiply() {
        var vars = run("x = 3 + 4 * 2");
        assertEquals(11, intVar(vars, "x"));
    }

    @Test
    void subtractAndDivide() {
        var vars = run("x = 20 - 8 / 4");
        assertEquals(18, intVar(vars, "x"));
    }

    @Test
    void parenthesisedArithmetic() {
        var vars = run("x = (3 + 4) * 2");
        assertEquals(14, intVar(vars, "x"));
    }

    @Test
    void multipleAssignments() {
        var vars = run("""
                a = 10
                b = 3
                c = a - b
                """);
        assertEquals(7, intVar(vars, "c"));
    }

    // --- comparisons ---

    @Test
    void equalityTrue() {
        var vars = run("""
                a = 5
                b = 5
                r = a == b
                """);
        assertTrue(boolVar(vars, "r"));
    }

    @Test
    void lessThanFalse() {
        var vars = run("""
                a = 7
                r = a < 3
                """);
        assertFalse(boolVar(vars, "r"));
    }

    // --- if / else ---

    @Test
    void ifTrueBranch() {
        var vars = run("""
                x = 0
                if true then x = 1 else x = 2
                """);
        assertEquals(1, intVar(vars, "x"));
    }

    @Test
    void ifFalseBranch() {
        var vars = run("""
                x = 0
                if false then x = 1 else x = 2
                """);
        assertEquals(2, intVar(vars, "x"));
    }

    @Test
    void ifWithComparison() {
        var vars = run("""
                a = 5
                b = 3
                x = 0
                if a > b then x = 100 else x = 0
                """);
        assertEquals(100, intVar(vars, "x"));
    }

    @Test
    void ifWithEqualityComparison() {
        var vars = run("""
                a = 4
                x = 0
                if a == 4 then x = 99 else x = 0
                """);
        assertEquals(99, intVar(vars, "x"));
    }

    @Test
    void ifNestedInElse() {
        // if-else chains using nested ifs
        var vars = run("""
                n = 2
                x = 0
                if n == 1 then x = 1 else if n == 2 then x = 2 else x = 3
                """);
        assertEquals(2, intVar(vars, "x"));
    }

    // --- while loops ---

    @Test
    void whileCountToFive() {
        var vars = run("""
                i = 0
                while i < 5 do i = i + 1
                """);
        assertEquals(5, intVar(vars, "i"));
    }

    @Test
    void whileAccumulatesSum() {
        // sum = 1 + 2 + ... + 10 = 55
        var vars = run("""
                i = 1
                sum = 0
                while i <= 10 do sum = sum + i, i = i + 1
                """);
        assertEquals(55, intVar(vars, "sum"));
        assertEquals(11, intVar(vars, "i"));
    }

    @Test
    void whileBodyNeverExecutedWhenConditionFalse() {
        var vars = run("""
                x = 42
                while false do x = 0
                """);
        assertEquals(42, intVar(vars, "x"));
    }

    @Test
    void whileMultipliesIteratively() {
        // 2^8 = 256 via repeated doubling
        var vars = run("""
                n = 8
                result = 1
                while n > 0 do result = result * 2, n = n - 1
                """);
        assertEquals(256, intVar(vars, "result"));
    }

    @Test
    void whileFindsDivisor() {
        // find the smallest divisor of 15 greater than 1
        var vars = run("""
                n = 15
                d = 2
                found = 0
                while found == 0 do if n / d * d == n then found = d else d = d + 1
                """);
        assertEquals(3, intVar(vars, "found"));
    }

    // --- functions ---

    @Test
    void simpleFunction() {
        var vars = run("""
                fun double(x) {
                return x * 2
                }
                result = double(7)
                """);
        assertEquals(14, intVar(vars, "result"));
    }

    @Test
    void functionWithTwoParameters() {
        var vars = run("""
                fun add(a, b) {
                return a + b
                }
                result = add(3, 4)
                """);
        assertEquals(7, intVar(vars, "result"));
    }

    @Test
    void functionWithThreeParameters() {
        var vars = run("""
                fun sum3(a, b, c) {
                return a + b + c
                }
                result = sum3(1, 2, 3)
                """);
        assertEquals(6, intVar(vars, "result"));
    }

    @Test
    void functionCallsAnotherFunction() {
        var vars = run("""
                fun square(n) {
                return n * n
                }
                fun sumOfSquares(a, b) {
                return square(a) + square(b)
                }
                result = sumOfSquares(3, 4)
                """);
        assertEquals(25, intVar(vars, "result"));
    }

    @Test
    void functionDefaultReturnIsZero() {
        // a function with no return statement returns 0
        var vars = run("""
                fun noop(x) {
                x = x + 1
                }
                result = noop(5)
                """);
        assertEquals(0, intVar(vars, "result"));
    }

    @Test
    void functionDoesNotLeakLocals() {
        // variables assigned inside a function are not visible outside
        var vars = run("""
                fun f(n) {
                inner = n * 2
                return inner
                }
                result = f(6)
                """);
        assertEquals(12, intVar(vars, "result"));
        assertFalse(vars.containsKey("inner"));
    }

    // --- recursion ---

    @Test
    void recursiveFactorial() {
        var vars = run("""
                fun fact(n) {
                if n <= 1 then return 1 else return n * fact(n - 1)
                }
                result = fact(5)
                """);
        assertEquals(120, intVar(vars, "result"));
    }

    @Test
    void recursiveFactorialBaseCase() {
        var vars = run("""
                fun fact(n) {
                if n <= 1 then return 1 else return n * fact(n - 1)
                }
                result = fact(1)
                """);
        assertEquals(1, intVar(vars, "result"));
    }

    @Test
    void recursiveFibonacci() {
        var vars = run("""
                fun fib(n) {
                if n <= 1 then return n else return fib(n - 1) + fib(n - 2)
                }
                result = fib(10)
                """);
        assertEquals(55, intVar(vars, "result"));
    }

    @Test
    void recursiveSumDownToZero() {
        // sum(n) = n + (n-1) + ... + 0
        var vars = run("""
                fun sum(n) {
                if n <= 0 then return 0 else return n + sum(n - 1)
                }
                result = sum(10)
                """);
        assertEquals(55, intVar(vars, "result"));
    }

    @Test
    void recursivePower() {
        // 2^10 = 1024
        var vars = run("""
                fun pow(base, exp) {
                if exp == 0 then return 1 else return base * pow(base, exp - 1)
                }
                result = pow(2, 10)
                """);
        assertEquals(1024, intVar(vars, "result"));
    }

    @Test
    void recursiveIsEven() {
        // two base cases: 0 -> even, 1 -> odd; reduce by 2 each step
        var vars = run("""
                fun isEven(n) {
                if n == 0 then return 1 else if n == 1 then return 0 else return isEven(n - 2)
                }
                r6 = isEven(6)
                r7 = isEven(7)
                """);
        assertEquals(1, intVar(vars, "r6"));
        assertEquals(0, intVar(vars, "r7"));
    }

    // --- error cases ---

    @Test
    void unboundVariableThrows() {
        assertThrows(InterpreterError.class, () -> run("x = y"));
    }

    @Test
    void divisionByZeroThrows() {
        assertThrows(InterpreterError.class, () -> run("""
                x = 1
                y = x / 0
                """));
    }

    @Test
    void argumentCountMismatchThrows() {
        assertThrows(InterpreterError.class, () -> run("""
                fun f(a, b) {
                return a + b
                }
                r = f(1)
                """));
    }

    @Test
    void callNonFunctionThrows() {
        assertThrows(InterpreterError.class, () -> run("""
                x = 5
                r = x(1)
                """));
    }

    @Test
    void stackOverflow() {
        assertThrows(InterpreterError.class, () -> run("""
                fun f() { return f() }
                _ = f()
                """));
    }

    @Test
    void functionUsedInArithmeticThrows() {
        assertThrows(InterpreterError.class, () -> run("""
                fun f(n) {
                return n
                }
                r = f + 1
                """));
    }

    @Test
    void variableBoundLaterThrows() {
        // y appears later in the program so it gets a slot, but reading it before
        // its assignment is reached throws because the slot still holds SentinelValue
        assertThrows(InterpreterError.class, () -> run("""
                x = y + 1
                y = 5
                """));
    }

    // --- Reader overload ---

    @Test
    void runFromReader() throws IOException {
        var source = """
                fun fact(n) {
                if n <= 1 then return 1 else return n * fact(n - 1)
                }
                result = fact(6)
                """;
        var vars = Interpreter.run(new StringReader(source)).getAllVariables();
        assertEquals(720, intVar(vars, "result"));
    }
}
