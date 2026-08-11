package org.task.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.task.dto.response.BookResponse;

import java.util.Optional;

@Component
public class BookClient {
    private final RestClient restClient;

    public BookClient(
            RestClient.Builder builder,
            @Value("${services.book-service.url}") String bookServiceUrl
    ) {
        this.restClient = builder.baseUrl(bookServiceUrl).build();
    }

    public Optional<BookResponse> findById(Long bookId) {
        return restClient.get()
                .uri("/api/books/{id}", bookId)
                .exchange((request, response) -> {
                    if (response.getStatusCode().is2xxSuccessful()) {
                        return Optional.ofNullable(response.bodyTo(BookResponse.class));
                    }
                    return Optional.empty();
                });
    }

    public boolean existsById(Long bookId) {
        return findById(bookId).isPresent();
    }
}
