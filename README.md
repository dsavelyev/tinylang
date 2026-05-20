## TinyLang
Tested on Oracle JDK 25. Uses ANTLR for parsing.

Project structure:

- `src/main/antlr4` - contains the ANTLR grammar
- `org.example.Main` - entry point
- `org.example.ast` - AST nodes, ANTLR visitor
- `org.example.interp` - interpreter core

Includes an IntelliJ run configuration. Can also be run with:
```
./mvnw compile
./mvnw exec:java
```

See `test/java/org.example/InterpreterTest.java` for example programs.