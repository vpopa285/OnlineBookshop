package org.task.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.task.model.Book;
import org.task.model.Review;
import org.task.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/db/initSchema.sql")
public class ReviewServiceTest {
    @Autowired
    ReviewService reviewService;
    @Autowired
    BookService bookService;
    @Autowired
    UserService userService;

    Book book = new Book("Test", "JP", "IT", "", 20);
    User user = new User("u", "e", "p");
    Review review = new Review(5, "nice", user);

    @Test
    void shouldCreateAndFindReview() {
        bookService.create(book);
        userService.create(user);
        reviewService.create(book.getId(), review);

        assertThat(reviewService.findByBookId(book.getId()))
                .anyMatch(r -> r.user().getId() == (user.getId()));
    }

    @Test
    @DirtiesContext
    void shouldDeleteReview() {
        bookService.create(book);
        userService.create(user);
        reviewService.create(book.getId(), review);

        reviewService.deleteByUserAndBookId(user.getId(), book.getId());

        assertThat(reviewService.findByBookId(book.getId())).isEmpty();
    }
}
