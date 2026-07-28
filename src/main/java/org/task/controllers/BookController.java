package org.task.controllers;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.task.dto.PageResponse;
import org.task.dto.filter.BookFilter;
import org.task.dto.request.BookRequest;
import org.task.dto.request.BookReviewRequest;
import org.task.dto.request.PriceUpdateRequest;
import org.task.dto.response.BookResponse;
import org.task.dto.response.ReviewResponse;
import org.task.service.BookService;
import org.task.service.ReviewService;

@RequiredArgsConstructor
@RestController
@RequestMapping({"/api/books", "/books"})
@Validated
public class BookController {
    private final BookService bookService;
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<PageResponse<BookResponse>> getBooks(
            @ModelAttribute BookFilter filter,
            @PageableDefault(
                    size = 20,
                    sort = "title"
            )
            Pageable pageable
            ) {
        return ResponseEntity.ok(bookService.findAllResponses(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBook(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(bookService.findResponseById(id));
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody BookRequest bookRequest
    ) {
        BookResponse createdBook = bookService.createResponse(bookRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable @Positive Long id,
            @Valid @RequestBody BookRequest bookRequest
    ) {
        return ResponseEntity.ok(bookService.update(id, bookRequest));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable @Positive Long id,
            @Valid @RequestBody PriceUpdateRequest request
    ) {
        return ResponseEntity.ok(bookService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @PathVariable @Positive Long id
    ) {
        bookService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{bookId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getBookReviews(
            @PathVariable @Positive Long bookId
    ) {
        return ResponseEntity.ok(reviewService.findResponsesByBookId(bookId));
    }

    @PostMapping("/{bookId}/reviews")
    public ResponseEntity<ReviewResponse> createBookReview(
            @PathVariable @Positive Long bookId,
            @Valid @RequestBody BookReviewRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createForBook(bookId, request));
    }

}
