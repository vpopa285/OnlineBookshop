package org.task.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.task.dto.request.BookReviewRequest;
import org.task.dto.request.UserReviewRequest;
import org.task.dto.response.ReviewResponse;

import java.util.List;
import java.util.Optional;

@Component
public class ReviewClient {
    private static final ParameterizedTypeReference<List<ReviewResponse>> REVIEW_LIST_TYPE =
            new ParameterizedTypeReference<>() { };

    private final RestClient restClient;

    public ReviewClient(
            RestClient.Builder builder,
            @Value("${services.book-service.url}") String bookServiceUrl
    ) {
        this.restClient = builder.baseUrl(bookServiceUrl).build();
    }

    public List<ReviewResponse> findByUserId(Long userId) {
        return restClient.get()
                .uri("/api/reviews/users/{userId}", userId)
                .retrieve()
                .body(REVIEW_LIST_TYPE);
    }

    public Optional<ReviewResponse> createForUser(Long userId, UserReviewRequest request) {
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
                    return Optional.empty();
                });
    }

    public void deleteByUserId(Long userId) {
        restClient.delete()
                .uri("/api/reviews/users/{userId}", userId)
                .retrieve()
                .toBodilessEntity();
    }
}
