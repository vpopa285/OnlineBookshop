package org.task.dto.response;

public record BookResponse(
        Long id,
        String title,
        String author,
        String genre,
        String content,
        double price
) { }
