package org.task.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class UserClient {
    private final RestClient restClient;

    public UserClient(
            @LoadBalanced
            RestClient.Builder builder,
            @Value("${clients.user-service.url}") String userServiceUrl
    ) {
        this.restClient = builder.baseUrl(userServiceUrl).build();
    }

    public boolean existsById(Long userId) {
        return restClient.get()
                .uri("/api/users/{id}", userId)
                .exchange((request, response) -> response.getStatusCode().is2xxSuccessful());
    }
}
