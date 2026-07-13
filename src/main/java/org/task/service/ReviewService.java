package org.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.task.model.Book;
import org.task.model.Review;
import org.task.repositories.BookRepository;
import org.task.repositories.ReviewRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;

    public void create(long bookId, Review review) {
        Optional<Book> book = bookRepository.findById(bookId);
        if(book.isPresent()) {
            review.setBook(book.get());
            reviewRepository.save(review);
        }
    }

    public List<Review> findByBookId(long bookId) {
        return reviewRepository.findAllByBookId(bookId);
    }

    public void deleteByUserAndBookId(long userId, long bookId) {
        reviewRepository.findByUserIdAndBookId(userId, bookId).ifPresent(reviewRepository::delete);
    }
}
