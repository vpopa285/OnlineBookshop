package org.task.dto.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import org.task.dto.filter.ReviewFilter;
import org.task.model.Review;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewSpecificationTest {

    @Test
    void shouldBuildExactRatePredicateWithEqualOperator() {
        ReviewFilter filter = new ReviewFilter();
        filter.setExactRate(5);
        Root<Review> root = mock();
        CriteriaQuery<?> query = mock();
        CriteriaBuilder criteriaBuilder = mock();
        Path<Integer> ratePath = mock();
        Predicate predicate = mock();

        when(root.<Integer>get("rate")).thenReturn(ratePath);
        when(criteriaBuilder.equal(ratePath, 5)).thenReturn(predicate);

        Specification<Review> specification = ReviewSpecification.getSpecification(filter);
        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(criteriaBuilder).equal(ratePath, 5);
    }
}
