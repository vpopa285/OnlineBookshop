package org.task;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.task.model.Book;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class LibraryTest {

    @Test
    void shouldAddBookToLibrary() {
        Library library = new Library();
        Book book = new Book("Java", "James", "Programming", "content", 10);

        library.getLibrary().add(book);

        assertThat(library.getLibrary()).hasSize(1).contains(book);
    }

    @Test
    void shouldFindBookById() {
        Library library = new Library();
        Book book = new Book("Java", "James", "Programming", "content", 10);

        library.getLibrary().add(book);

        Optional<Book> result = library.findById(book.getId());

        assertThat(result).isPresent().contains(book);
    }

    @Test
    void shouldReturnEmptyWhenBookNotFoundById() {
        Library library = new Library();

        Optional<Book> result = library.findById(999);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest
    @CsvSource({
            "TITLE, Java, 1, true, false, false",
            "AUTHOR, james, 2, true, false, true",
            "GENRE, Programming, 3, true, true, true",
            "TITLE, Go, 0, false, false, false"
    })
    void shouldSearchByTitle(SearchType searchType, String value, int expectedSize, boolean containsBook1, boolean containsBook2, boolean containsBook3) {
        Library library = new Library();

        Book book1 = new Book("Java", "James", "Programming", "content", 10);
        Book book2 = new Book("Python", "Guido", "Programming", "content", 10);
        Book book3 = new Book("R", "James", "Programming", "content", 10);

        library.getLibrary().add(book1);
        library.getLibrary().add(book2);
        library.getLibrary().add(book3);

        Set<Book> result = library.search(searchType, value);

        assertThat(result).hasSize(expectedSize);

        assertThat(result.contains(book1)).isEqualTo(containsBook1);
        assertThat(result.contains(book2)).isEqualTo(containsBook2);
        assertThat(result.contains(book3)).isEqualTo(containsBook3);
    }

}
