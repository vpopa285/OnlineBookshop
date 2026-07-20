package org.task.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.task.dto.BookRequest;
import org.task.dto.BookResponse;
import org.task.dto.BookReviewRequest;
import org.task.dto.PriceUpdateRequest;
import org.task.dto.ReviewResponse;
import org.task.service.BookService;
import org.task.service.ReviewService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping({"/api/books", "/books"})
public class BookController {
    private final BookService bookService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<BookResponse>> getBooks() {
        return ResponseEntity.ok(bookService.findAllResponses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBook(
            @PathVariable Long id
    ) {
        return ResponseEntity.of(bookService.findResponseById(id));
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(
            @RequestBody BookRequest bookRequest
    ) {
        BookResponse createdBook = bookService.createResponse(bookRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @RequestBody BookRequest bookRequest
    ) {
        return ResponseEntity.ok(bookService.update(id, bookRequest));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @RequestBody PriceUpdateRequest request
    ) {
        return ResponseEntity.of(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable Long id
    ) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getBookReviews(
            @PathVariable Long bookId
    ) {
        return ResponseEntity.ok(reviewService.findResponsesByBookId(bookId));
    }

    @PostMapping("/{bookId}/reviews")
    public ResponseEntity<ReviewResponse> createBookReview(
            @PathVariable Long bookId,
            @RequestBody BookReviewRequest request
    ) {
        return reviewService.createForBook(bookId, request)
                .map(review -> ResponseEntity.status(HttpStatus.CREATED).body(review))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
