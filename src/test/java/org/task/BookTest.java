package org.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class BookTest {

    @Test
    void hashCodeTest() {
        Book b1 = new Book("A", "B", "C", "...", 1);
        Book b2 = new Book("A", "B", "C", "...", 1);

        assertThat(b1.hashCode()).isNotEqualTo(b2.hashCode());
    }

    static Stream<Arguments> notEqualCases() {
        return Stream.of(
                Arguments.of(
                        new Book("A", "B", "C", "...", 1),
                        new Book("A", "B", "C", "...", 1)
                ),
                Arguments.of(
                        new Book("A", "B", "C", "...", 1),
                        new Object()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("notEqualCases")
    void notEqualTest(Object first, Object second) {
        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void viewBookTest() {
        Book book = new Book("A", "B", "C", "...", 1);

        String result = book.viewBook();

        assertThat(result).isEqualTo("A" +
                                        "\nby: B" +
                                        "\n\n...\n");
    }
}
