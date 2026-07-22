package org.task.dto;

public record OrderRequest(
        Long userId,
        Long bookId
) { }
