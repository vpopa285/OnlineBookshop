package org.task.service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.task.dto.BookReviewRequest;
import org.task.dto.ReviewResponse;
import org.task.dto.ReviewUpdateRequest;
import org.task.dto.UserReviewRequest;
import org.task.exceptions.BookNotFoundException;
import org.task.exceptions.ReviewNotFoundException;
import org.task.exceptions.UserNotFoundException;
import org.task.model.Book;
import org.task.model.Review;
import org.task.model.User;
import org.task.repositories.BookRepository;
import org.task.repositories.ReviewRepository;
import org.task.repositories.UserRepository;

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

    public ReviewResponse findResponseById(Long id) {
        return reviewRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ReviewNotFoundException(id)
        );
    }

    public List<ReviewResponse> findResponsesByBookId(Long bookId) {
        bookRepository.findById(bookId).orElseThrow(() -> new BookNotFoundException(bookId));

        return reviewRepository.findAllByBookId(bookId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ReviewResponse> findResponsesByUserId(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        return reviewRepository.findAllByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ReviewResponse createForBook(Long bookId, BookReviewRequest request) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        Review review = Review.builder()
                .book(book)
                .user(user)
                .rate(request.rate())
                .comment(request.comment())
                .build();

        return toResponse(reviewRepository.save(review));
    }

    public ReviewResponse createForUser(Long userId, UserReviewRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new BookNotFoundException(request.bookId()));

        Review review = Review.builder()
                .user(user)
                .book(book)
                .rate(request.rate())
                .comment(request.comment())
                .build();

        return toResponse(reviewRepository.save(review));
    }

    public ReviewResponse update(Long id, ReviewUpdateRequest request) {
        return reviewRepository.findById(id)
                .map(review -> Review.builder()
                        .id(review.getId())
                        .user(review.getUser())
                        .book(review.getBook())
                        .rate(request.rate())
                        .comment(request.comment())
                        .build())
                .map(reviewRepository::save)
                .map(this::toResponse)
                .orElseThrow(() -> new ReviewNotFoundException(id));
    }

    public void deleteById(Long id) {
        Review review = reviewRepository.findById(id).orElseThrow(() -> new ReviewNotFoundException(id));
        reviewRepository.delete(review);
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
