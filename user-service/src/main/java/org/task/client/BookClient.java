package org.task.client;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.task.dto.response.BookResponse;
import org.task.exceptions.ExternalServiceUnavailableException;

import java.util.Optional;
import java.util.function.Supplier;

@Component
public class BookClient {
    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final ServiceTokenProvider serviceTokenProvider;

    public BookClient(
            @LoadBalanced
            RestClient.Builder builder,
            @Value("${clients.book-service.url}") String bookServiceUrl,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory,
            RetryRegistry retryRegistry,
            ServiceTokenProvider serviceTokenProvider
    ) {
        this.restClient = builder.baseUrl(bookServiceUrl).build();
        this.circuitBreaker = circuitBreakerFactory.create("bookService");
        this.retry = retryRegistry.retry("bookServiceRead");
        this.serviceTokenProvider = serviceTokenProvider;
    }

    public Optional<BookResponse> findById(Long bookId) {
        Supplier<Optional<BookResponse>> bookLookup =
                Retry.decorateSupplier(retry, () -> doFindById(bookId));
        return circuitBreaker.run(bookLookup, this::bookServiceUnavailable);
    }

    private Optional<BookResponse> doFindById(Long bookId) {
        return restClient.get()
                .uri("/api/books/{id}", bookId)
                .headers(headers -> headers.setBearerAuth(serviceTokenProvider.token()))
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return Optional.ofNullable(response.bodyTo(BookResponse.class));
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

    private Optional<BookResponse> bookServiceUnavailable(Throwable cause) {
        throw new ExternalServiceUnavailableException("book-service", cause);
    }

    public boolean existsById(Long bookId) {
        return findById(bookId).isPresent();
    }
}
