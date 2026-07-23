package org.task.dto;

import jakarta.validation.constraints.Positive;

public record PriceUpdateRequest(
        @Positive(message = "Price must be positive")
        double price
) { }
