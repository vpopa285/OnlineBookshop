package org.task.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int currentPage,
        int pageSize,
        int numberOfElements,
        long totalElements,
        int totalPages,
        Integer maxPageNumber,
        boolean first,
        boolean last,
        List<String> sort
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        List<String> sortParameters = page.getSort()
                .stream()
                .map(order ->
                        order.getProperty()
                                + ","
                                + order.getDirection()
                                .name()
                                .toLowerCase()
                )
                .toList();

        Integer maxPageNumber = page.getTotalPages() == 0
                ? null
                : page.getTotalPages() - 1;

        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getNumberOfElements(),
                page.getTotalElements(),
                page.getTotalPages(),
                maxPageNumber,
                page.isFirst(),
                page.isLast(),
                sortParameters
        );
    }
}
