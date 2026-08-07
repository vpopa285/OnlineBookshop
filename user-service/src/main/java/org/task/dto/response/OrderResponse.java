package org.task.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        UserResponse user,
        List<BookResponse> books,
        LocalDateTime createdAt
) { }
