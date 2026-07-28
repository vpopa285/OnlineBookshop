package org.task.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record ReviewUpdateRequest(
        @Positive(message = "Rate must be positive")
        @Max(value = 5, message = "Rate must be at most 5")
        int rate,
        @NotBlank(message = "Comment is required")
        String comment
) { }
