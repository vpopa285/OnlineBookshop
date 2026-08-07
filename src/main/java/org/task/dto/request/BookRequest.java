package org.task.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record BookRequest(
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "Author is required")
        String author,
        @NotBlank(message = "Genre is required")
        String genre,
        @NotBlank(message = "Content is required")
        String content,
        @Positive(message = "Price must be positive")
        double price
) { }
