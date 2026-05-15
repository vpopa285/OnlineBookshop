package org.task;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.task.model.Book;

import static org.assertj.core.api.Assertions.assertThat;

class SearchTypeTest {

    private final Book book =
            new Book("Java", "James", "Programming", "content", 10);

    @ParameterizedTest
    @CsvSource({
            "TITLE, Java",
            "AUTHOR, James",
            "GENRE, Programming"
    })
    void shouldExtractSpecifiedTypeTest(SearchType searchType, String output) {
        String result = searchType.extract(book);

        assertThat(result).isNotNull().isEqualTo(output);
    }

}
