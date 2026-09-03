package org.task.controllers;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.task.dto.PageResponse;
import org.task.dto.filter.ReviewFilter;
import org.task.dto.request.ReviewUpdateRequest;
import org.task.dto.response.ReviewResponse;
import org.task.service.ReviewService;

@RequiredArgsConstructor
@RestController
@RequestMapping({"/api/reviews", "/reviews"})
@Validated
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ReviewResponse>> getReviews(
            @ModelAttribute ReviewFilter filter,
            @PageableDefault(size = 20) Pageable pageable
            ) {
        return ResponseEntity.ok(reviewService.findAllResponses(filter, pageable));
    }

    @GetMapping("/{reviewId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'SERVICE')")
    public ResponseEntity<ReviewResponse> getReview(
            @PathVariable @Positive Long reviewId
    ) {
        return ResponseEntity.ok(reviewService.findResponseById(reviewId));
    }

    @PatchMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')"
            + " or @securityAuthorization.isReviewOwner(#reviewId, authentication)")
    public ResponseEntity<ReviewResponse> updateReview(
            @PathVariable @Positive Long reviewId,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(reviewService.update(reviewId, request));
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')"
            + " or @securityAuthorization.isReviewOwner(#reviewId, authentication)")
    public ResponseEntity<Void> deleteReview(
            @PathVariable @Positive Long reviewId
    ) {
        reviewService.deleteById(reviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')"
            + " or @securityAuthorization.isSelf(#userId, authentication)")
    public ResponseEntity<List<ReviewResponse>> getReviewsByUser(
            @PathVariable @Positive Long userId
    ) {
        return ResponseEntity.ok(reviewService.findResponsesByUserId(userId));
    }

    @DeleteMapping("/users/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')")
    public ResponseEntity<Void> deleteReviewsByUser(
            @PathVariable @Positive Long userId
    ) {
        reviewService.deleteByUserId(userId);
        return ResponseEntity.noContent().build();
    }
}
