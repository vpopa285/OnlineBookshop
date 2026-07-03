package org.task.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.task.jdbc.JdbcExecutor;
import org.task.model.Book;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/db/initSchema.sql")
public class BookServiceTest {
    @Autowired
    BookService bookService;

    @Autowired
    JdbcExecutor jdbcExecutor;

    Book book = new Book("Test", "JP", "IT", "", 20);

    @Test
    void shouldCreateAndFindBook() {
        bookService.create(book);

        Book found = bookService.findById(book.getId());

        assertThat(found.getId()).isEqualTo(book.getId());
        assertThat(found.getTitle()).isEqualTo("Test");
    }

    //Dirty context example
    @Test
    //without @DirtiesContext the test will not pass
    @DirtiesContext
    void shouldCreateAndFindOnlyOneBook() {
        bookService.create(book);

        Book updated = new Book(book.getId(), "Update", "LM", "IT", "", 10);
        bookService.update(updated);

        List<Book> foundBooks = bookService.findAll();

        assertThat(foundBooks.size()).isEqualTo(1);
    }

    @Test
    @DirtiesContext
    void shouldUpdateBook() {
        bookService.create(book);

        Book updated = new Book(book.getId(), "Update", "LM", "IT", "", 10);
        bookService.update(updated);

        Book found = bookService.findById(book.getId());

        assertThat(updated.getTitle()).isEqualTo(found.getTitle());
    }

    @Test
    @DirtiesContext
    void shouldDeleteBook() {
        bookService.create(book);

        bookService.deleteById(book.getId());

        Book extract = bookService.findById(book.getId());

        assertThat(extract).isNull();
    }

    @Test
    @DirtiesContext
    void shouldLoadBookWithReviews() {
        bookService.create(book);

        jdbcExecutor.execute("INSERT INTO users(id, username, email, password, amount, restriction) VALUES (1, 'u', 'e', 'p', 0, false)");
        jdbcExecutor.execute("INSERT INTO reviews(id, book_id, user_id, rating, comment) VALUES (1, ?, 1, 5, 'good')", book.getId());

        Book result = bookService.findByIdWithReviews(book.getId());

        assertThat(result.getReviews().size()).isEqualTo(1);
    }
}