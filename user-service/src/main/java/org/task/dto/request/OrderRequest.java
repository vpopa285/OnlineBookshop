package org.task.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderRequest(
        @NotNull(message = "User id is required")
        @Positive(message = "User id must be positive")
        Long userId,
        @NotNull(message = "Book id is required")
        @Positive(message = "Book id must be positive")
        Long bookId
) { }
