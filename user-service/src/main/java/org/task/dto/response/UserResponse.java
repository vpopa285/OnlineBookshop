package org.task.dto.response;

public record UserResponse(
        Long id,
        String username,
        String email,
        boolean restriction
) { }
