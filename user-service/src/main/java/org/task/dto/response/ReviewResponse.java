package org.task.dto.response;

public record ReviewResponse(
        Long id,
        Long userId,
        Long bookId,
        int rate,
        String comment
) { }
