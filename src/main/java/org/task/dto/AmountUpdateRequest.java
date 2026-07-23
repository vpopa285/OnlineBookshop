package org.task.dto;

import jakarta.validation.constraints.Positive;

public record AmountUpdateRequest(
        @Positive(message = "Amount must be positive")
        double price
) { }
