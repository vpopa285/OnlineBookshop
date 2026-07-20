package org.task.dto;

public record UserRequest(
        String username,
        String email,
        String password
) { }
