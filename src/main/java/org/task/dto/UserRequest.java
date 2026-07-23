package org.task.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotBlank(message = "Incorrect name")
        String username,
        @NotBlank(message = "Incorrect email")
        @Email(message = "Incorrect email")
        String email,
        @NotBlank(message = "Incorrect password")
        @Size(min = 8, message = "Password must contain at least 8 characters")
        String password
) { }
