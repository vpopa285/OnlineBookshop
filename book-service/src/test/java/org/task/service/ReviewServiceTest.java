package org.task.service;

import org.junit.jupiter.api.Test;
import org.task.client.UserClient;
import org.task.dto.request.BookReviewRequest;
import org.task.exceptions.UserNotFoundException;
import org.task.model.Book;
import org.task.model.Review;
import org.task.repositories.BookRepository;
import org.task.repositories.ReviewRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewServiceTest {
    private final ReviewRepository reviewRepository = mock(ReviewRepository.class);
    private final BookRepository bookRepository = mock(BookRepository.class);
    private final UserClient userClient = mock(UserClient.class);
    private final ReviewService reviewService = new ReviewService(
            reviewRepository,
            bookRepository,
            userClient
    );

    @Test
    void shouldCreateReviewWhenBookExistsAndUserExistsByHttp() {
        Book book = Book.builder()
                .id(10L)
                .title("Book")
                .author("Author")
                .genre("Genre")
                .content("content")
                .price(20)
                .build();

        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(userClient.existsById(5L)).thenReturn(true);
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review review = invocation.getArgument(0);
            review.setId(1L);
            return review;
        });

        var response = reviewService.createForBook(
                10L,
                new BookReviewRequest(5L, 4, "good")
        );

        assertThat(response.userId()).isEqualTo(5L);
        assertThat(response.bookId()).isEqualTo(10L);
    }

    @Test
    void shouldNotCreateReviewWhenUserDoesNotExistByHttp() {
        when(bookRepository.findById(10L)).thenReturn(Optional.of(Book.builder().id(10L).build()));
        when(userClient.existsById(5L)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.createForBook(
                10L,
                new BookReviewRequest(5L, 4, "good")
        )).isInstanceOf(UserNotFoundException.class);
        verify(reviewRepository, never()).save(any(Review.class));
    }
}
