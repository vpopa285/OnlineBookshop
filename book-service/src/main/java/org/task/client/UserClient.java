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
import org.task.exceptions.ExternalServiceUnavailableException;

import java.util.function.Supplier;

@Component
public class UserClient {
    private final RestClient restClient;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public UserClient(
            @LoadBalanced
            RestClient.Builder builder,
            @Value("${clients.user-service.url}") String userServiceUrl,
            CircuitBreakerFactory<?, ?> circuitBreakerFactory,
            RetryRegistry retryRegistry
    ) {
        this.restClient = builder.baseUrl(userServiceUrl).build();
        this.circuitBreaker = circuitBreakerFactory.create("userService");
        this.retry = retryRegistry.retry("userServiceRead");
    }

    public boolean existsById(Long userId) {
        Supplier<Boolean> userLookup = Retry.decorateSupplier(retry, () -> doExistsById(userId));
        return circuitBreaker.run(userLookup, this::userServiceUnavailable);
    }

    private boolean doExistsById(Long userId) {
        return restClient.get()
                .uri("/api/users/{id}", userId)
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return true;
                    }
                    if (response.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                        return false;
                    }
                    throw new ExternalServiceUnavailableException(
                            "user-service",
                            response.getStatusCode().value()
                    );
                });
    }

    private boolean userServiceUnavailable(Throwable cause) {
        throw new ExternalServiceUnavailableException("user-service", cause);
    }
}
