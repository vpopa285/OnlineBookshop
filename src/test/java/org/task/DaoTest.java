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
        Book book = bookDao.findById(1).orElse(null);

        assertThat(book).isNotNull();
        assertThat(book.getTitle()).isEqualTo("The Great Gatsby");
        assertThat(book.getGenre()).isEqualTo("Classic");
        assertThat(book.getPrice()).isEqualTo(12.99);
    }

    @Test
    void returnNullForMissingBookTest() {
        assertThat(bookDao.findById(-100)).isEmpty();
    }

    @Test
    void CRUDTest() {

        Book book = Book.builder()
                .title("Refactoring")
                .author("Martin Fowler")
                .genre("Programming")
                .content("Code design.")
                .price(45)
                .build();

        bookDao.create(book);

        Long id = book.getId();

        assertThat(bookDao.findById(id).get().getTitle())
                .isEqualTo("Refactoring");

        Book updated = Book.builder()
                .id(id)
                .title("Refactoring")
                .author("Martin Fowler")
                .genre("Programming")
                .content("Improving existing code.")
                .price(40)
                .build();

        bookDao.update(updated);

        assertThat(bookDao.findById(id).get().getContent())
                .isEqualTo("Improving existing code.");

        assertThat(bookDao.findById(id).get().getPrice())
                .isEqualTo(40);

        bookDao.deleteById(id);

        assertThat(bookDao.findById(id)).isEmpty();
    }

    @Test
    void listBooksTest() {
        List<Book> books = bookDao.findAll();

        assertThat(books).extracting(Book::getTitle)
                .contains("The Great Gatsby", "1984", "Clean Code");
    }

    @Test
    void fetchBookTest() {
        Book book = bookDao.findByIdWithReviews(1).get();

        assertThat(book).isNotNull();
        assertThat(book.getReviews()).hasSize(2);
        assertThat(book.getReviews()).extracting(Review::getComment)
                .containsExactly("An absolute masterpiece.", "Great atmosphere.");
        assertThat(book.getReviews()).extracting(review -> review.getUser().getUsername())
                .containsExactly("alice_reads", "bob_pages");
    }

    @Test
    void fetchBookWithoutReviewsTest() {
        Book book = bookDao.findByIdWithReviews(3).get();

        assertThat(book).isNotNull();
        assertThat(book.getReviews()).isEmpty();
    }

    @Test
    void returnNullTest() {
        Book book = bookDao.findByIdWithReviews(-100).orElse(null);

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
        User user = userDao.findById(1).orElse(null);
        reviewDao.create(3, Review.builder()
                .rate(4)
                .comment("Useful and practical.")
                .user(user)
                .build());

        assertThat(reviewDao.findByBookId(3)).extracting(Review::getComment)
                .contains("Useful and practical.");

        reviewDao.deleteByUserAndBookId(1, 3);
        assertThat(reviewDao.findByBookId(3)).extracting(Review::getComment)
                .doesNotContain("Useful and practical.");
    }

    @Test
    void createUpdateAndDeleteUserTest() {

        User user = User.builder()
                .username("new_reader")
                .email("new@example.com")
                .password("secret")
                .amount(25)
                .restriction(false)
                .build();

        userDao.create(user);

        Long id = user.getId();

        assertThat(userDao.findById(id).get().getUsername())
                .isEqualTo("new_reader");

        user.setAmount(50);
        user.setRestriction(true);

        userDao.update(user);

        User updated = userDao.findById(id).get();

        assertThat(updated.getAmount()).isEqualTo(50);
        assertThat(updated.isRestriction()).isTrue();

        userDao.deleteById(id);

        assertThat(userDao.findById(id)).isEmpty();
    }

    @Test
    void rejectDuplicateUserEmailTest() {

        User user1 = User.builder()
                .username("alice")
                .email("alice@example.com")
                .password("secret")
                .amount(10)
                .restriction(false)
                .build();

        userDao.create(user1);

        User duplicate = User.builder()
                .username("someone")
                .email("alice@example.com")
                .password("secret")
                .amount(10)
                .restriction(false)
                .build();

        assertThatThrownBy(() -> userDao.create(duplicate))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create user");
    }

    @Test
    void rejectInvalidReviewRatingTest() {
        User user = userDao.findById(1).get();

        assertThatThrownBy(() -> reviewDao.create(2, Review.builder()
                .rate(6)
                .comment("Too high")
                .user(user)
                .build()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to create review");
    }
}
