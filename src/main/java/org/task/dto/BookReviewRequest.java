package org.task.dto;

public record BookReviewRequest(
        Long userId,
        int rate,
        String comment
) { }
