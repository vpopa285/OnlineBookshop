package org.task.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CurrentUserOrderRequest(
        @NotNull(message = "Book id is required")
        @Positive(message = "Book id must be positive")
        Long bookId
) { }
