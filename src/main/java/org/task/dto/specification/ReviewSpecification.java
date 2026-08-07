package org.task.dto.specification;

import org.springframework.data.jpa.domain.Specification;

import org.task.dto.filter.ReviewFilter;
import org.task.model.Review;

import java.util.ArrayList;
import java.util.List;

public class ReviewSpecification {
    public static Specification<Review> getSpecification(ReviewFilter filter) {
        List<Specification<Review>> specifications = new ArrayList<>();

        if (filter.getMinRate() != null) {
            specifications.add(
                    minRateContains(filter.getMinRate())
            );
        }

        if (filter.getMaxRate() != null) {
            specifications.add(
                    maxRateContains(filter.getMaxRate())
            );
        }

        if (filter.getExactRate() != null) {
            specifications.add(
                    exactRateContains(filter.getExactRate())
            );
        }

        return specifications.stream()
                .reduce(Specification::and)
                .orElseGet(ReviewSpecification::empty);
    }

    private static Specification<Review> empty() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.conjunction();
    }

    private static Specification<Review> minRateContains(Integer minRate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.<Integer>get("rate"),
                        minRate
                );
    }

    private static Specification<Review> maxRateContains(Integer maxRate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.<Integer>get("rate"),
                        maxRate
                );
    }

    private static Specification<Review> exactRateContains(Integer exactRate) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.<Integer>get("rate"),
                        exactRate
                );
    }
}
