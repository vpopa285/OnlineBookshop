package org.task.controllers;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
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
import org.task.dto.request.CurrentUserOrderRequest;
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

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponse> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.findResponseById(currentUserId(authentication)));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponse> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UserRequest userRequest
    ) {
        return ResponseEntity.ok(userService.update(currentUserId(authentication), userRequest));
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserResponse> updateCurrentUserAmount(
            Authentication authentication,
            @Valid @RequestBody AmountUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.update(currentUserId(authentication), request));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteCurrentUser(Authentication authentication) {
        userService.deleteById(currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<ReviewResponse>> getCurrentUserReviews(
            Authentication authentication
    ) {
        Long userId = currentUserId(authentication);
        userService.findResponseById(userId);
        return ResponseEntity.ok(reviewClient.findByUserId(userId));
    }

    @PostMapping("/me/reviews")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewResponse> createCurrentUserReview(
            Authentication authentication,
            @Valid @RequestBody UserReviewRequest request
    ) {
        Long userId = currentUserId(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewClient.createForUser(userId, request)
                        .orElseThrow(() -> new BookNotFoundException(request.bookId())));
    }

    @GetMapping("/me/orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<OrderResponse>> getCurrentUserOrders(
            Authentication authentication
    ) {
        return ResponseEntity.ok(orderService.findResponsesByUserId(currentUserId(authentication)));
    }

    @PostMapping("/me/orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> createCurrentUserOrder(
            Authentication authentication,
            @Valid @RequestBody CurrentUserOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createForUser(currentUserId(authentication), request.bookId()));
    }

    @GetMapping("/me/orders/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<OrderResponse> getCurrentUserOrder(
            Authentication authentication,
            @PathVariable @Positive Long orderId
    ) {
        return ResponseEntity.ok(orderService.findResponseByUserIdAndOrderId(
                currentUserId(authentication),
                orderId
        ));
    }

    @GetMapping("/me/books")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BookReadResponse>> getCurrentUserBooks(
            Authentication authentication
    ) {
        return ResponseEntity.ok(userService.findReadableBooks(currentUserId(authentication)));
    }

    @GetMapping("/me/books/{bookId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookReadResponse> readCurrentUserBook(
            Authentication authentication,
            @PathVariable @Positive Long bookId
    ) {
        return ResponseEntity.ok(userService.findReadableBook(
                currentUserId(authentication),
                bookId
        ));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasAnyRole('ADMIN', 'SERVICE')"
            + " or @securityAuthorization.isSelf(#id, authentication)")
    public ResponseEntity<UserResponse> getUser(
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(userService.findResponseById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody UserRequest userRequest
    ) {
        UserResponse createdUser = userService.createResponse(userRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.isSelf(#id, authentication)")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UserRequest userRequest
    ) {
        return ResponseEntity.ok(userService.update(id, userRequest));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.isSelf(#id, authentication)")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable @Positive Long id,
            @Valid @RequestBody AmountUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.isSelf(#id, authentication)")
    public ResponseEntity<Void> deleteUser(
            @PathVariable @Positive Long id
    ) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userId}/reviews")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.isSelf(#userId, authentication)")
    public ResponseEntity<List<ReviewResponse>> getUserReviews(
            @PathVariable @Positive Long userId
    ) {
        userService.findResponseById(userId);
        return ResponseEntity.ok(reviewClient.findByUserId(userId));
    }

    @PostMapping("/{userId}/reviews")
    @PreAuthorize("@securityAuthorization.isSelf(#userId, authentication)")
    public ResponseEntity<ReviewResponse> createUserReview(
            @PathVariable @Positive Long userId,
            @Valid @RequestBody UserReviewRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reviewClient.createForUser(userId, request)
                        .orElseThrow(() -> new BookNotFoundException(request.bookId())));
    }

    @GetMapping("/{userId}/orders")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.isSelf(#userId, authentication)")
    public ResponseEntity<List<OrderResponse>> getUserOrders(
            @PathVariable @Positive Long userId
    ) {
        return ResponseEntity.ok(orderService.findResponsesByUserId(userId));
    }

    @GetMapping("/{userId}/orders/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.isSelf(#userId, authentication)")
    public ResponseEntity<OrderResponse> getUserOrder(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long orderId
    ) {
        return ResponseEntity.ok(orderService.findResponseByUserIdAndOrderId(userId, orderId));
    }

    @GetMapping("/{userId}/books")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.isSelf(#userId, authentication)")
    public ResponseEntity<List<BookReadResponse>> getUserBooks(
            @PathVariable @Positive Long userId
    ) {
        return ResponseEntity.ok(userService.findReadableBooks(userId));
    }

    @GetMapping("/{userId}/books/{bookId}")
    @PreAuthorize("hasRole('ADMIN') or @securityAuthorization.isSelf(#userId, authentication)")
    public ResponseEntity<BookReadResponse> readUserBook(
            @PathVariable @Positive Long userId,
            @PathVariable @Positive Long bookId
    ) {
        return ResponseEntity.ok(userService.findReadableBook(userId, bookId));
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccessDeniedException("JWT token must contain a user_id claim");
        }

        Object userId = jwt.getClaims().get("user_id");
        if (userId instanceof Number number) {
            return number.longValue();
        }
        if (userId instanceof String value) {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException ex) {
                throw new AccessDeniedException("JWT token must contain a valid user_id claim", ex);
            }
        }

        throw new AccessDeniedException("JWT token must contain a user_id claim");
    }
}
