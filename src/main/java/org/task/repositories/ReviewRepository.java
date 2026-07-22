package org.task.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.task.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByBookId(Long id);
    List<Review> findAllByUserId(Long id);
    Optional<Review> findByUserIdAndBookId(Long userId, Long bookId);
    void deleteAllByUserId(Long userId);
}
