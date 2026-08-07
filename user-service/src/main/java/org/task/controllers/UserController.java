package org.task.controllers;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.task.client.ReviewClient;
import org.task.dto.PageResponse;
import org.task.dto.filter.UserFilter;
import org.task.dto.request.AmountUpdateRequest;
import org.task.dto.request.UserRequest;
import org.task.dto.request.UserReviewRequest;
import org.task.dto.response.BookReadResponse;
import org.task.dto.response.OrderResponse;
import org.task.dto.response.ReviewResponse;
import org.task.dto.response.UserResponse;
import org.task.exceptions.BookNotFoundException;
import org.task.service.OrderService;
import org.task.service.UserService;

@RestController
@RequestMapping({"/api/users", "/users"})
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;
    private final ReviewClient reviewClient;
    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<PageResponse<UserResponse>> getUsers(
            @ModelAttribute UserFilter filter,
            @PageableDefault(
                    size = 20,
                    sort = "username"
            )
            Pageable pageable
            ) {
        return ResponseEntity.ok(userService.findAllResponses(filter, pageable));
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
        userService.findResponseById(userId);
        return ResponseEntity.ok(reviewClient.findByUserId(userId));
    }

    @PostMapping("/{userId}/reviews")
    public ResponseEntity<ReviewResponse> createUserReview(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody UserReviewRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewClient.createForUser(userId, request)
                        .orElseThrow(() -> new BookNotFoundException(request.bookId())));
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
