package org.task.dto.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import org.task.dto.filter.BookFilter;
import org.task.model.Book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookSpecificationTest {

    @Test
    void shouldBuildMinimumPricePredicateWithGreaterThanOrEqualOperator() {
        BookFilter filter = new BookFilter();
        filter.setPriceMin(12.99);
        Root<Book> root = mock();
        CriteriaQuery<?> query = mock();
        CriteriaBuilder criteriaBuilder = mock();
        Path<Double> pricePath = mock();
        Predicate predicate = mock();

        when(root.<Double>get("price")).thenReturn(pricePath);
        when(criteriaBuilder.greaterThanOrEqualTo(pricePath, 12.99)).thenReturn(predicate);

        Specification<Book> specification = BookSpecification.withFilter(filter);
        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(criteriaBuilder).greaterThanOrEqualTo(pricePath, 12.99);
    }

    @Test
    void shouldBuildMaximumPricePredicateWithLessThanOrEqualOperator() {
        BookFilter filter = new BookFilter();
        filter.setPriceMax(30.00);
        Root<Book> root = mock();
        CriteriaQuery<?> query = mock();
        CriteriaBuilder criteriaBuilder = mock();
        Path<Double> pricePath = mock();
        Predicate predicate = mock();

        when(root.<Double>get("price")).thenReturn(pricePath);
        when(criteriaBuilder.lessThanOrEqualTo(pricePath, 30.00)).thenReturn(predicate);

        Specification<Book> specification = BookSpecification.withFilter(filter);
        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(criteriaBuilder).lessThanOrEqualTo(pricePath, 30.00);
    }
}
