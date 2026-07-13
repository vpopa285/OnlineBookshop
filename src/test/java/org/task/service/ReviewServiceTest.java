package org.task.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.task.BookshopApplication;
import org.task.model.Book;
import org.task.model.Review;
import org.task.model.User;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BookshopApplication.class)
@ActiveProfiles("test")
@Sql(scripts = "/db/initSchema.sql")
public class ReviewServiceTest {
    @Autowired
    ReviewService reviewService;
    @Autowired
    BookService bookService;
    @Autowired
    UserService userService;

    Book book = Book.builder()
            .title("Test")
            .author("JP")
            .genre("IT")
            .content("")
            .price(20)
            .build();

    User user = User.builder()
            .username("u")
            .email("e")
            .password("p")
            .build();
    Review review = Review.builder()
            .rate(5)
            .comment("nice")
            .user(user)
            .build();

    @Test
    void shouldCreateAndFindReview() {
        bookService.create(book);
        userService.create(user);
        reviewService.create(book.getId(), review);

        assertThat(reviewService.findByBookId(book.getId()))
                .anyMatch(r -> r.getUser().getId().equals(user.getId()));
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
