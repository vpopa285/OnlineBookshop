package org.task.dto;

public record UserResponse(
        String username,
        String email,
        boolean restriction
) { }
