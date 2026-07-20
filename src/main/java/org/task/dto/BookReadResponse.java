package org.task.dto;

public record BookReadResponse(
        Long id,
        String title,
        String author,
        String genre,
        String content
) { }
