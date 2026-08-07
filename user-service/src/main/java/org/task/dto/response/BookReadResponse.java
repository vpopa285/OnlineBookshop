package org.task.dto.response;

public record BookReadResponse(
        Long id,
        String title,
        String author,
        String genre,
        String content
) { }
