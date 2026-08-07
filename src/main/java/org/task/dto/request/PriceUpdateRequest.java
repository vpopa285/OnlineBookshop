package org.task.dto.request;

import jakarta.validation.constraints.Positive;

public record PriceUpdateRequest(
        @Positive(message = "Price must be positive")
        double price
) { }
