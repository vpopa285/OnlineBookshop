package org.task.repositories;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.task.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    @EntityGraph(attributePaths = {"reviews", "reviews.user"})
    Optional<Book> findWithReviewsById(Long id);

    List<Book> searchBooksByAuthor(String author);
    List<Book> searchBooksByTitle(String title);
    List<Book> searchBooksByGenre(String genre);
    boolean existsByTitle(String title);
}
