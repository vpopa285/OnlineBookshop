package org.task.client;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.task.dto.request.BookReviewRequest;
import org.task.dto.request.UserReviewRequest;
import org.task.dto.response.ReviewResponse;
import org.task.exceptions.ExternalServiceUnavailableException;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class ReviewClient {
    private static final ParameterizedTypeReference<List<ReviewResponse>> REVIEW_LIST_TYPE =
            new ParameterizedTypeReference<>() { };

    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry readRetry;
    private final Retry deleteRetry;

    public ReviewClient(
            @LoadBalanced
            RestClient.Builder builder,
            @Value("${clients.book-service.url}") String bookServiceUrl,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory,
            RetryRegistry retryRegistry
    ) {
        this.restClient = builder.baseUrl(bookServiceUrl).build();
        this.circuitBreaker = circuitBreakerFactory.create("bookReviewService");
        this.readRetry = retryRegistry.retry("bookReviewServiceRead");
        this.deleteRetry = retryRegistry.retry("bookReviewServiceDelete");
    }

    public List<ReviewResponse> findByUserId(Long userId) {
        Supplier<List<ReviewResponse>> reviewLookup =
                Retry.decorateSupplier(readRetry, () -> doFindByUserId(userId));
        return circuitBreaker.run(reviewLookup, this::bookServiceUnavailable);
    }

    private List<ReviewResponse> doFindByUserId(Long userId) {
        return restClient.get()
                .uri("/api/reviews/users/{userId}", userId)
                .retrieve()
                .body(REVIEW_LIST_TYPE);
    }

    public Optional<ReviewResponse> createForUser(Long userId, UserReviewRequest request) {
        return circuitBreaker.run(
                () -> doCreateForUser(userId, request),
                this::bookServiceUnavailable
        );
    }

    private Optional<ReviewResponse> doCreateForUser(Long userId, UserReviewRequest request) {
        BookReviewRequest bookReviewRequest = new BookReviewRequest(
                userId,
                request.rate(),
                request.comment()
        );

        return restClient.post()
                .uri("/api/books/{bookId}/reviews", request.bookId())
                .body(bookReviewRequest)
                .exchange((httpRequest, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return Optional.ofNullable(response.bodyTo(ReviewResponse.class));
                    }
                    if (response.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                        return Optional.empty();
                    }
                    throw new ExternalServiceUnavailableException(
                            "book-service",
                            response.getStatusCode().value()
                    );
                });
    }

    public void deleteByUserId(Long userId) {
        Supplier<Void> reviewDeletion = Retry.decorateSupplier(deleteRetry, () -> {
            doDeleteByUserId(userId);
            return null;
        });
        circuitBreaker.run(reviewDeletion, this::bookServiceUnavailable);
    }

    private void doDeleteByUserId(Long userId) {
        restClient.delete()
                .uri("/api/reviews/users/{userId}", userId)
                .retrieve()
                .toBodilessEntity();
    }

    private <T> T bookServiceUnavailable(Throwable cause) {
        throw new ExternalServiceUnavailableException("book-service", cause);
    }
}
