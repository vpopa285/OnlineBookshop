package org.task.dto.response;

public record UserResponse(
        String username,
        String email,
        boolean restriction
) { }
