package org.task.dto;

import java.util.List;

public record OrderResponse(
        Long id,
        Long userId,
        List<Long> bookIds
) { }
