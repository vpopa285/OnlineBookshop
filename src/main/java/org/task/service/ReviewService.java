package org.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.task.dto.BookReviewRequest;
import org.task.dto.ReviewResponse;
import org.task.dto.ReviewUpdateRequest;
import org.task.dto.UserReviewRequest;
import org.task.model.Book;
import org.task.model.Review;
import org.task.model.User;
import org.task.repositories.BookRepository;
import org.task.repositories.ReviewRepository;
import org.task.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

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

    public List<ReviewResponse> findAllResponses() {
        return reviewRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<ReviewResponse> findResponseById(Long id) {
        return reviewRepository.findById(id)
                .map(this::toResponse);
    }

    public List<ReviewResponse> findResponsesByBookId(Long bookId) {
        return reviewRepository.findAllByBookId(bookId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ReviewResponse> findResponsesByUserId(Long userId) {
        return reviewRepository.findAllByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<ReviewResponse> createForBook(Long bookId, BookReviewRequest request) {
        Optional<Book> book = bookRepository.findById(bookId);
        Optional<User> user = userRepository.findById(request.userId());
        if (book.isEmpty() || user.isEmpty()) {
            return Optional.empty();
        }

        Review review = Review.builder()
                .book(book.get())
                .user(user.get())
                .rate(request.rate())
                .comment(request.comment())
                .build();

        return Optional.of(toResponse(reviewRepository.save(review)));
    }

    public Optional<ReviewResponse> createForUser(Long userId, UserReviewRequest request) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Book> book = bookRepository.findById(request.bookId());
        if (user.isEmpty() || book.isEmpty()) {
            return Optional.empty();
        }

        Review review = Review.builder()
                .user(user.get())
                .book(book.get())
                .rate(request.rate())
                .comment(request.comment())
                .build();

        return Optional.of(toResponse(reviewRepository.save(review)));
    }

    public Optional<ReviewResponse> update(Long id, ReviewUpdateRequest request) {
        return reviewRepository.findById(id)
                .map(review -> Review.builder()
                        .id(review.getId())
                        .user(review.getUser())
                        .book(review.getBook())
                        .rate(request.rate())
                        .comment(request.comment())
                        .build())
                .map(reviewRepository::save)
                .map(this::toResponse);
    }

    public void deleteById(Long id) {
        reviewRepository.deleteById(id);
    }

    public void deleteByUserAndBookId(long userId, long bookId) {
        reviewRepository.findByUserIdAndBookId(userId, bookId).ifPresent(reviewRepository::delete);
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUser().getId(),
                review.getBook().getId(),
                review.getRate(),
                review.getComment()
        );
    }
}
