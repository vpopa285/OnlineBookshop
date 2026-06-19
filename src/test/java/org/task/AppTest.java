package org.task;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AppTest {

    @Autowired
    private App app;

    private ByteArrayOutputStream output;
    private PrintStream originalOut;
    private InputStream originalIn;

    @BeforeEach
    void setUp() {
        output = new ByteArrayOutputStream();
        originalOut = System.out;
        originalIn = System.in;

        System.setOut(new PrintStream(output));
        app.init();

    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
        System.setIn(originalIn);
    }

    private void provideInput(String data) {
        System.setIn(new ByteArrayInputStream(data.getBytes()));
        app.initScanner();
    }

    private void run(String input) {
        provideInput(input);

        app.runMenu();
    }

    static Stream<Arguments> singleRunCases() {
        return Stream.of(
                Arguments.of("1\nAUTHOR\nJames\n-1\n0", "Java"),
                Arguments.of("1\nGENRE\nProgramming\n-1\n0", "Python"),
                Arguments.of("1\nTITLE\nNonExistentBook\n0", "No books found"),
                Arguments.of("1\nWRONGTYPE\n0", "Invalid search type"),
                Arguments.of("1\nTITLE\nJava\n999\n0", "Invalid book id"),
                Arguments.of("2\n0\n0", "Not enough money"),
                Arguments.of("2\n99\n0", "Book not found"),
                Arguments.of("4\n0\n0", "You don't own this book"),
                Arguments.of("5\n0\n0", "You don't own this book"),
                Arguments.of("6\na\n0", "Invalid number"),
                Arguments.of("4\na\n0", "Invalid number"),
                Arguments.of("6\n0\n0", "Invalid amount"),
                Arguments.of("6\n-50\n0", "Invalid amount"),
                Arguments.of("7\n4\n0", "Total books"),
                Arguments.of("2\n1\n5\n1\n9\n0", "Invalid rating"),
                Arguments.of("7\n3\n9999\n0", "User not found"),
                Arguments.of("99\n0", "Invalid option")
        );
    }

    @ParameterizedTest
    @MethodSource("singleRunCases")
    void singleRunTests(String input, String expected) {
        run(input);
        assertThat(output.toString()).contains(expected);
    }

    static Stream<Arguments> multiStepCases() {
        return Stream.of(
                Arguments.of("2\n1\n0||5\n1\n5\nGreat book\n0||1\nTITLE\nPython\n1\n0", new String[]{"Great book", "user", "5"}),
                Arguments.of("2\n1\n0||2\n1\n0", new String[]{"Python"}),
                Arguments.of("2\n1\n0||3\n0", new String[]{"Python"}),
                Arguments.of("2\n1\n0||4\n1\n0", new String[]{"Python content..."}),
                Arguments.of("7\n1\nRust\nSteve\nSystems\nRust content\n30\n0||1\nTITLE\nRust\n-1\n0", new String[]{"Rust"}),
                Arguments.of("2\n1\n0||7\n3\n1\n0", new String[]{})
        );
    }

    @ParameterizedTest
    @MethodSource("multiStepCases")
    void multiStepTests(String steps, String[] expectedParts) {
        String[] parts = steps.split("\\|\\|");
        for (String p : parts) {
            run(p);
        }
        String out = output.toString();
        for (String expected : expectedParts) {
            assertThat(out).contains(expected);
        }
    }
}
