package org.task.controllers;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.task.dto.AmountUpdateRequest;
import org.task.dto.BookReadResponse;
import org.task.dto.OrderResponse;
import org.task.dto.ReviewResponse;
import org.task.dto.UserReviewRequest;
import org.task.dto.UserRequest;
import org.task.dto.UserResponse;
import org.task.service.OrderService;
import org.task.service.ReviewService;
import org.task.service.UserService;

@RestController
@RequestMapping({"/api/users", "/users"})
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;
    private final ReviewService reviewService;
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(userService.findAllResponses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(userService.findResponseById(id));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest userRequest
    ) {
        UserResponse createdUser = userService.createResponse(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UserRequest userRequest
    ) {
        return ResponseEntity.ok(userService.update(id, userRequest));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable @Positive Long id,
            @Valid @RequestBody AmountUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(
            @PathVariable @Positive Long id
    ) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/reviews")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(
            @PathVariable @Positive Long userId
    ) {
        return ResponseEntity.ok(reviewService.findResponsesByUserId(userId));
    }

    @PostMapping("/{userId}/reviews")
    public ResponseEntity<ReviewResponse> createUserReview(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody UserReviewRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewService.createForUser(userId, request));
    }

    @GetMapping("/{userId}/orders")
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @PathVariable @Positive Long userId
    ) {
        return ResponseEntity.ok(orderService.findResponsesByUserId(userId));
    }

    @GetMapping("/{userId}/books/{bookId}")
    public ResponseEntity<BookReadResponse> readUserBook(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long bookId
    ) {
        return ResponseEntity.ok(userService.findReadableBook(userId, bookId));
    }

}
