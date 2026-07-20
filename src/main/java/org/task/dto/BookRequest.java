package org.task.dto;

public record BookRequest(
        String title,
        String author,
        String genre,
        String content,
        double price
) { }
