package org.task;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.task.dao.BookDao;
import org.task.dao.ReviewDao;
import org.task.dao.UserDao;
import org.task.model.Book;
import org.task.model.Review;
import org.task.model.User;
import org.task.jdbc.JdbcExecutor;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DaoTest {
    private static BookDao bookDao;
    private static ReviewDao reviewDao;
    private static UserDao userDao;

    @BeforeAll
    static void setUpDatabase() {
        DatabaseTestSupport.runInitScript();
        JdbcExecutor jdbcExecutor = DatabaseTestSupport.JDBC_EXECUTOR;

        bookDao = new BookDao(jdbcExecutor);
        reviewDao = new ReviewDao(jdbcExecutor);
        userDao = new UserDao(jdbcExecutor);
    }

    @Test
    void preloadedBookTest() {
        Book book = bookDao.findById(1);

        assertThat(book).isNotNull();
        assertThat(book.getTitle()).isEqualTo("The Great Gatsby");
        assertThat(book.getGenre()).isEqualTo("Classic");
        assertThat(book.getPrice()).isEqualTo(12.99);
    }

    @Test
    void returnNullForMissingBookTest() {
        assertThat(bookDao.findById(-100)).isNull();
    }

    @Test
    void CRUDTest() {
        Book book = new Book(201, "Refactoring", "Martin Fowler", "Programming", "Code design.", 45);

        bookDao.create(book);
        assertThat(bookDao.findById(201).getTitle()).isEqualTo("Refactoring");

        Book updated = new Book(201, "Refactoring", "Martin Fowler", "Programming", "Improving existing code.", 40);
        bookDao.update(updated);
        assertThat(bookDao.findById(201).getContent()).isEqualTo("Improving existing code.");
        assertThat(bookDao.findById(201).getPrice()).isEqualTo(40);

        bookDao.deleteById(201);
        assertThat(bookDao.findById(201)).isNull();
    }

    @Test
    void listBooksTest() {
        List<Book> books = bookDao.findAll();

        assertThat(books).extracting(Book::getTitle)
                .contains("The Great Gatsby", "1984", "Clean Code");
    }

    @Test
    void fetchBookTest() {
        Book book = bookDao.findByIdWithReviews(1);

        assertThat(book).isNotNull();
        assertThat(book.getReviews()).hasSize(2);
        assertThat(book.getReviews()).extracting(Review::comment)
                .containsExactly("An absolute masterpiece.", "Great atmosphere.");
        assertThat(book.getReviews()).extracting(review -> review.user().getUsername())
                .containsExactly("alice_reads", "bob_pages");
    }

    @Test
    void fetchBookWithoutReviewsTest() {
        Book book = bookDao.findByIdWithReviews(3);

        assertThat(book).isNotNull();
        assertThat(book.getReviews()).isEmpty();
    }

    @Test
    void returnNullTest() {
        Book book = bookDao.findByIdWithReviews(-100);

        assertThat(book).isNull();
    }

    @Test
    void mapBookWrapsSqlExceptionTest() throws NoSuchMethodException {
        Method mapBook = BookDao.class.getDeclaredMethod("mapBook", ResultSet.class);
        mapBook.setAccessible(true);

        ResultSet resultSet = (ResultSet) Proxy.newProxyInstance(
                ResultSet.class.getClassLoader(),
                new Class[]{ResultSet.class},
                (proxy, method, args) -> {
                    throw new SQLException("Broken result set");
                }
        );

        assertThatThrownBy(() -> mapBook.invoke(null, resultSet))
                .satisfies(throwable -> assertThat(throwable.getCause())
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessage("Failed to map book")
                        .hasCauseInstanceOf(SQLException.class));
    }

    @Test
    void createAndDeleteReviewTest() {
        User user = userDao.findById(1);
        reviewDao.create(3, new Review(4, "Useful and practical.", user));

        assertThat(reviewDao.findByBookId(3)).extracting(Review::comment)
                .contains("Useful and practical.");

        reviewDao.deleteByUserAndBookId(1, 3);
        assertThat(reviewDao.findByBookId(3)).extracting(Review::comment)
                .doesNotContain("Useful and practical.");
    }

    @Test
    void createUpdateAndDeleteUserTest() {
        User user = new User(301, "new_reader", "new@example.com", "secret", 25, false);

        userDao.create(user);
        assertThat(userDao.findById(301).getUsername()).isEqualTo("new_reader");

        user.addFunds(50);
        user.setRestriction(true);
        userDao.update(user);

        User updated = userDao.findById(301);
        assertThat(updated.getAmount()).isEqualTo(75);
        assertThat(updated.isRestriction());

        userDao.deleteById(301);
        assertThat(userDao.findById(301)).isNull();
    }

    @Test
    void rejectDuplicateUserEmailTest() {
        User duplicate = new User(302, "someone", "alice@example.com", "secret", 10, false);

        assertThatThrownBy(() -> userDao.create(duplicate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to execute the statement");
    }

    @Test
    void rejectInvalidReviewRatingTest() {
        User user = userDao.findById(1);

        assertThatThrownBy(() -> reviewDao.create(2, new Review(6, "Too high.", user)))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to execute the statement");
    }
}
