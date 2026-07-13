package org.task;

import jakarta.persistence.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.task.model.Book;
import org.task.model.Review;
import org.task.model.User;
import org.task.repositories.BookRepository;
import org.task.repositories.ReviewRepository;
import org.task.repositories.UserRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Sql(scripts = "/db/initSchema.sql")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ExperimentTest {

    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    EntityManagerFactory emf;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    ReviewRepository reviewRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanUp() {
        reviewRepository.deleteAll();
        bookRepository.deleteAll();
    }

    private Book newBook() {
        return Book.builder()
                .title("Test")
                .author("JP")
                .genre("IT")
                .content("")
                .price(10)
                .build();
    }

    private Review newReview(Book book) {
        User user = User.builder()
                .username("ua")
                .email("em")
                .password("pass")
                .build();

        user = userRepository.save(user);

        return Review.builder()
                .user(user)
                .book(book)
                .rate(5)
                .comment("com")
                .build();
    }

    @Test
    void saveParentWithoutID_repository() {
        Book saved = bookRepository.save(newBook());

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void saveParentWithoutID_persist() {
        Book book = newBook();

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        em.persist(book);

        tx.commit();

        assertThat(book.getId()).isNotNull();

        em.close();
    }

    @Test
    void saveParentWithoutID_merge() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        Book managed = em.merge(newBook());

        tx.commit();

        assertThat(managed.getId()).isNotNull();

        em.close();
    }

    @Test
    void saveParentWithID_repository() {
        Book book = newBook();
        book.setId(1L);

        assertThatThrownBy(() -> bookRepository.save(book))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void saveParentWithID_persist() {
        Book book = newBook();
        book.setId(1L);

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        assertThatThrownBy(() -> em.persist(book)).isInstanceOf(PersistenceException.class);

        tx.rollback();

        em.close();
    }

    @Test
    void saveParentWithID_merge() {
        Book book = newBook();
        book.setId(1L);

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        assertThatThrownBy(() -> em.merge(book))
                .isInstanceOf(PersistenceException.class);

        tx.rollback();

        em.close();
    }

    @Test
    void saveParentsWithSameID_repository() {
        Book book = bookRepository.save(newBook());

        book.setId(1L);
        book.setTitle("Updated");

        Book saved = bookRepository.save(book);

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void saveParentsWithSameID_persist() {
        Book book = newBook();

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        em.persist(book);

        book.setId(1L);
        book.setTitle("Updated");

        em.persist(book);

        tx.commit();

        assertThat(book.getId()).isNotNull();

        em.close();
    }

    @Test
    void saveParentsWithSameID_merge() {
        Book book = newBook();

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        em.merge(book);

        book.setId(1L);
        book.setTitle("Updated");

        Book managed = em.merge(book);

        tx.commit();

        assertThat(managed.getId()).isNotNull();

        em.close();
    }

    @Test
    void saveParentAndChildren_repository() {
        Book book = newBook();

        Review review = newReview(book);

        Book savedBook = bookRepository.save(book);
        Review savedReview = reviewRepository.save(review);

        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedReview.getId()).isNotNull();
    }

    @Test
    void saveParentAndChildren_persist() {
        Book book = newBook();
        Review review = newReview(book);

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        em.persist(book);
        em.persist(review);

        tx.commit();

        assertThat(book.getId()).isNotNull();
        assertThat(review.getId()).isNotNull();

        em.close();
    }

    @Test
    void saveParentAndChildren_merge() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        Book managedBook = em.merge(newBook());
        Review review = newReview(managedBook);
        Review managedReview = em.merge(review);

        tx.commit();

        assertThat(managedBook.getId()).isNotNull();
        assertThat(managedReview.getId()).isNotNull();

        em.close();
    }

    @Test
    void saveParentWithExistingChildren_repository() {
        Book book = bookRepository.save(newBook());
        Review review = reviewRepository.save(newReview(book));

        book.setTitle("Updated");
        book.setReviews(List.of(review));

        Book saved = bookRepository.save(book);

        assertThat(saved.getReviews()).hasSize(1);
    }

    @Test
    void saveParentWithExistingChildren_persist() {
        Book book = bookRepository.save(newBook());
        Review review = reviewRepository.save(newReview(book));

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        book.setTitle("Updated");
        book.setReviews(List.of(review));

        assertThatThrownBy(() -> em.persist(book))
                .isInstanceOf(PersistenceException.class);

        tx.rollback();

        em.close();
    }

    @Test
    void saveParentWithExistingChildren_merge() {
        Book book = bookRepository.save(newBook());
        Review review = reviewRepository.save(newReview(book));

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        book.setTitle("Updated");
        book.setReviews(List.of(review));

        Book managed = em.merge(book);

        tx.commit();

        assertThat(managed.getReviews()).hasSize(1);

        em.close();
    }

    @Test
    void saveChildWithoutParent_repository() {
        Review review = newReview(null);
        review.setBook(null);

        assertThatThrownBy(() -> reviewRepository.save(review))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void saveChildWithoutParent_persist() {
        Review review = newReview(null);
        review.setBook(null);

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        assertThatThrownBy(() -> {
            em.persist(review);
            em.flush();
        }).isInstanceOf(PersistenceException.class);

        tx.rollback();

        em.close();
    }

    @Test
    void saveChildWithoutParent_merge() {
        Review review = newReview(null);
        review.setBook(null);

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        assertThatThrownBy(() -> {
            em.merge(review);
            em.flush();
        }).isInstanceOf(PersistenceException.class);

        tx.rollback();

        em.close();
    }

    @Test
    void saveChildWithParentNotInDatabase_repository() {
        Book book = newBook();
        book.setId(999L);

        Review review = newReview(book);

        assertThatThrownBy(() -> reviewRepository.save(review))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void saveChildWithParentNotInDatabase_persist() {
        Book book = newBook();
        book.setId(999L);

        Review review = newReview(book);

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        assertThatThrownBy(() -> {
            em.persist(review);
            em.flush();
        }).isInstanceOf(PersistenceException.class);

        tx.rollback();

        em.close();
    }

    @Test
    void saveChildWithParentNotInDatabase_merge() {
        Book book = newBook();
        book.setId(999L);

        Review review = newReview(book);

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        assertThatThrownBy(() -> {
            em.merge(review);
            em.flush();
        }).isInstanceOf(PersistenceException.class);

        tx.rollback();

        em.close();
    }

    @Test
    void saveChildWithDetachedParent_repository() {
        Book book = bookRepository.save(newBook());

        Review saved = reviewRepository.save(newReview(book));

        assertThat(saved.getId()).isNotNull();
    }

    @Test
    void saveChildWithDetachedParent_persist() {
        Book book = bookRepository.save(newBook());

        Review review = newReview(book);

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        em.persist(review);

        tx.commit();

        assertThat(review.getId()).isNotNull();

        em.close();
    }

    @Test
    void saveChildWithDetachedParent_merge() {
        Book book = bookRepository.save(newBook());

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        Review managed = em.merge(newReview(book));

        tx.commit();

        assertThat(managed.getId()).isNotNull();

        em.close();
    }

    @Test
    void updateParentWithoutExplicitSave_usingDirtyChecking() {
        Book book = bookRepository.save(newBook());

        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        tx.begin();

        Book managed = em.find(Book.class, book.getId());
        managed.setTitle("Updated");

        em.flush();

        String titleInDb = getTitleFromDB(book);

        assertThat(titleInDb).isNotEqualTo("Updated");

        tx.commit();

        String updatedTitleInDb = getTitleFromDB(book);

        assertThat(updatedTitleInDb).isEqualTo("Updated");

        em.close();
    }

    private String getTitleFromDB(Book book) {
        return jdbcTemplate.queryForObject(
                "SELECT title FROM books WHERE id = ?",
                String.class,
                book.getId()
        );
    }
}
