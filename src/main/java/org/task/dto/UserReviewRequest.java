package org.task.dto;

public record UserReviewRequest(
        Long bookId,
        int rate,
        String comment
) { }
