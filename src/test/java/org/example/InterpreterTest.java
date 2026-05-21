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
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterTest {

    private static Map<String, Value> run(String source) {
        var interp = new Interpreter();
        interp.run(source);
        return interp.getAllVariables();
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

    // --- unary operators ---

    @Test
    void unaryNegateInteger() {
        var vars = run("x = -5");
        assertEquals(-5, intVar(vars, "x"));
    }

    @Test
    void unaryNegateExpression() {
        var vars = run("x = -(3 + 4)");
        assertEquals(-7, intVar(vars, "x"));
    }

    @Test
    void unaryNegateOnNonIntThrows() {
        // - applied to a non-integer value should throw TYPE_MISMATCH
        assertEquals(InterpreterError.Kind.TYPE_MISMATCH, errorKind(() -> run("""
                fun f() { return 0 }
                x = -f
                """)));
    }

    @Test
    void notTrue() {
        var vars = run("x = !true");
        assertFalse(boolVar(vars, "x"));
    }

    @Test
    void notBoolExpression() {
        var vars = run("""
                a = 3
                b = 5
                x = !(a == b)
                """);
        assertTrue(boolVar(vars, "x"));
    }

    @Test
    void notInteger() {
        // any non-zero int is truthy, so !nonzero is false
        var vars = run("x = !1");
        assertFalse(boolVar(vars, "x"));
    }

    // --- logical connectives ---

    @Test
    void andFalseLeft() {
        var vars = run("x = false && true");
        assertFalse(boolVar(vars, "x"));
    }

    @Test
    void orFalseLeft() {
        var vars = run("x = false || true");
        assertTrue(boolVar(vars, "x"));
    }

    @Test
    void andConvertsIntOperands() {
        // non-zero is truthy, zero is falsy
        var vars = run("x = 1 && 1");
        assertTrue(boolVar(vars, "x"));
    }

    @Test
    void andShortCircuitsOnFalseLeft() {
        // rhs divides by zero - must not be evaluated
        var vars = run("""
                x = false && 1 / 0 == 0
                """);
        assertFalse(boolVar(vars, "x"));
    }

    @Test
    void orShortCircuitsOnTrueLeft() {
        var vars = run("""
                x = true || 1 / 0 == 0
                """);
        assertTrue(boolVar(vars, "x"));
    }

    @Test
    void andDoesNotShortCircuitOnTrueLeft() {
        // rhs is evaluated, so division by zero propagates
        assertEquals(InterpreterError.Kind.DIVISION_BY_ZERO,
                errorKind(() -> run("x = true && 1 / 0 == 0")));
    }

    @Test
    void orDoesNotShortCircuitOnFalseLeft() {
        assertEquals(InterpreterError.Kind.DIVISION_BY_ZERO,
                errorKind(() -> run("x = false || 1 / 0 == 0")));
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

    // --- compound statements and stmtGroups ---

    @Test
    void compoundStatementExecutesBody() {
        var vars = run("""
                x = 0
                {
                x = 99
                }
                """);
        assertEquals(99, intVar(vars, "x"));
    }

    @Test
    void compoundStatementSharesEnclosingScope() {
        // assignments inside a curly block land in the surrounding scope, not a new one
        var vars = run("""
                x = 0
                {
                x = 7
                }
                y = x + 1
                """);
        assertEquals(7, intVar(vars, "x"));
        assertEquals(8, intVar(vars, "y"));
    }

    @Test
    void compoundStatementWithMultipleLines() {
        var vars = run("""
                x = 0
                {
                x = 1
                x = x + 1
                x = x * 3
                }
                """);
        assertEquals(6, intVar(vars, "x"));
    }

    @Test
    void compoundStatementAsThenBranch() {
        var vars = run("""
                x = 0
                y = 0
                if true then {
                x = 1
                y = 2
                } else x = 99
                """);
        assertEquals(1, intVar(vars, "x"));
        assertEquals(2, intVar(vars, "y"));
    }

    @Test
    void compoundStatementAsElseBranch() {
        var vars = run("""
                x = 0
                y = 0
                if false then x = 99 else {
                x = 3
                y = 4
                }
                """);
        assertEquals(3, intVar(vars, "x"));
        assertEquals(4, intVar(vars, "y"));
    }

    @Test
    void compoundStatementAsWhileBody() {
        // curly block is a valid statement, so it can serve as the stmtGroup of a while
        var vars = run("""
                i = 0
                x = 0
                while i < 3 do {
                i = i + 1
                x = x + i
                }
                """);
        // iteration 1: i=1, x=1; iteration 2: i=2, x=3; iteration 3: i=3, x=6
        assertEquals(3, intVar(vars, "i"));
        assertEquals(6, intVar(vars, "x"));
    }

    @Test
    void stmtGroupExecutesLeftToRight() {
        // within a comma group the left assignment is visible to the right side
        var vars = run("x = 1, x = x + 10");
        assertEquals(11, intVar(vars, "x"));
    }

    @Test
    void stmtGroupWithThreeStatements() {
        var vars = run("a = 1, b = 2, c = a + b");
        assertEquals(1, intVar(vars, "a"));
        assertEquals(2, intVar(vars, "b"));
        assertEquals(3, intVar(vars, "c"));
    }

    @Test
    void stmtGroupInsideCompoundStatement() {
        // comma-separated group can appear as a line inside a curly block
        var vars = run("""
                x = 0
                y = 0
                {
                x = 5, y = x * 2
                }
                """);
        assertEquals(5, intVar(vars, "x"));
        assertEquals(10, intVar(vars, "y"));
    }

    @Test
    void commaTerminatesIfInsideWhileStmtGroup() {
        // parses as: while ... do (if i==1 then x=x+1 else x=x+10), y=y+1, i=i+1
        // the comma after the else-branch cuts off the if; y and i updates always run
        var vars = run("""
                i = 0
                x = 0
                y = 0
                while i < 3 do if i == 1 then x = x + 1 else x = x + 10, y = y + 1, i = i + 1
                """);
        // i=0: else -> x=10,  y=1, i=1
        // i=1: then -> x=11,  y=2, i=2
        // i=2: else -> x=21,  y=3, i=3
        assertEquals(21, intVar(vars, "x"));
        assertEquals(3, intVar(vars, "y"));
        assertEquals(3, intVar(vars, "i"));
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

    private static InterpreterError.Kind errorKind(Executable program) {
        return assertThrows(InterpreterError.class, program).kind();
    }

    @Test
    void unboundVariableThrows() {
        assertEquals(InterpreterError.Kind.UNBOUND_VARIABLE, errorKind(() -> run("x = y")));
    }

    @Test
    void divisionByZeroThrows() {
        assertEquals(InterpreterError.Kind.DIVISION_BY_ZERO, errorKind(() -> run("""
                x = 1
                y = x / 0
                """)));
    }

    @Test
    void argumentCountMismatchThrows() {
        assertEquals(InterpreterError.Kind.ARGUMENT_COUNT_MISMATCH, errorKind(() -> run("""
                fun f(a, b) {
                return a + b
                }
                r = f(1)
                """)));
    }

    @Test
    void callNonFunctionThrows() {
        assertEquals(InterpreterError.Kind.TYPE_MISMATCH, errorKind(() -> run("""
                x = 5
                r = x(1)
                """)));
    }

    @Test
    void stackOverflow() {
        assertEquals(InterpreterError.Kind.STACK_OVERFLOW, errorKind(() -> run("""
                fun f() { return f() }
                _ = f()
                """)));
    }

    @Test
    void functionUsedInArithmeticThrows() {
        assertEquals(InterpreterError.Kind.TYPE_MISMATCH, errorKind(() -> run("""
                fun f(n) {
                return n
                }
                r = f + 1
                """)));
    }

    @Test
    void variableBoundLaterThrows() {
        // y appears later in the program so it gets a slot, but reading it before
        // its assignment is reached throws because the slot's value is still null
        assertEquals(InterpreterError.Kind.UNBOUND_VARIABLE, errorKind(() -> run("""
                x = y + 1
                y = 5
                """)));
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
        var interp = new Interpreter();
        interp.run(new StringReader(source));
        var vars = interp.getAllVariables();
        assertEquals(720, intVar(vars, "result"));
    }
}
