package org.task.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Incorrect username")
        String username,

        @NotBlank(message = "Incorrect password")
        String password
) {
}
