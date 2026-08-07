package org.task.controllers;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.task.dto.ReviewResponse;
import org.task.dto.ReviewUpdateRequest;
import org.task.service.ReviewService;

@RequiredArgsConstructor
@RestController
@RequestMapping({"/api/reviews", "/reviews"})
@Validated
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<List<ReviewResponse>> getReviews() {
        return ResponseEntity.ok(reviewService.findAllResponses());
    }

    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(
            @PathVariable @Positive Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.findResponseById(reviewId));
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable @Positive Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(reviewService.update(reviewId, request));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable @Positive Long reviewId
    ) {
        reviewService.deleteById(reviewId);
        return ResponseEntity.noContent().build();
    }
}
