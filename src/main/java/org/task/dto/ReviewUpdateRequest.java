package org.task.dto;

public record ReviewUpdateRequest(
        int rate,
        String comment
) { }
