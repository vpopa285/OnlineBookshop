package org.task.dto.specification;

import io.micrometer.common.util.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.task.dto.filter.OrderFilter;
import org.task.model.Order;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {
    public static Specification<Order> getSpecification(OrderFilter filter) {
        List<Specification<Order>> specifications = new ArrayList<>();

        if (StringUtils.isNotBlank(filter.getUsername())) {
            specifications.add(
                    usernameContains(filter.getUsername())
            );
        }

        if (filter.getMinDate() != null) {
            specifications.add(
                minDateContains(filter.getMinDate())
            );
        }

        if (filter.getMaxDate() != null) {
            specifications.add(
                    maxDateContains(filter.getMaxDate())
            );
        }

        if (filter.getExactDate() != null) {
            specifications.add(
                    exactDateContains(filter.getExactDate())
            );
        }

        return specifications.stream()
                .reduce(Specification::and)
                .orElseGet(OrderSpecification::empty);
    }

    private static Specification<Order> empty() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.conjunction();
    }

    private static Specification<Order> usernameContains(String username) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                root.join("user").get("username")
                        ),
                        "%" + username.toLowerCase() + "%"
                );
    }

    private static Specification<Order> minDateContains(LocalDateTime minDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("createdAt"),
                        minDate
                );
    }

    private static Specification<Order> maxDateContains(LocalDateTime maxDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("createdAt"),
                        maxDate
                );
    }

    private static Specification<Order> exactDateContains(LocalDateTime exactDate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("createdAt"),
                        exactDate
                );
    }
}
