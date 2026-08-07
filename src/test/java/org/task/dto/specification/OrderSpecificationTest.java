package org.task.dto.specification;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;
import org.task.dto.filter.OrderFilter;
import org.task.model.Order;
import org.task.model.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderSpecificationTest {

    @Test
    void shouldBuildUsernamePredicateThroughUserJoin() {
        OrderFilter filter = new OrderFilter();
        filter.setUsername("Alice");
        Root<Order> root = mock();
        CriteriaQuery<?> query = mock();
        CriteriaBuilder criteriaBuilder = mock();
        Join<Order, User> userJoin = mock();
        Path<String> usernamePath = mock();
        Expression<String> lowerUsername = mock();
        Predicate predicate = mock();

        when(root.<Order, User>join("user")).thenReturn(userJoin);
        when(userJoin.<String>get("username")).thenReturn(usernamePath);
        when(criteriaBuilder.lower(usernamePath)).thenReturn(lowerUsername);
        when(criteriaBuilder.like(lowerUsername, "%alice%")).thenReturn(predicate);

        Specification<Order> specification = OrderSpecification.getSpecification(filter);
        Predicate result = specification.toPredicate(root, query, criteriaBuilder);

        assertThat(result).isSameAs(predicate);
        verify(root).join("user");
        verify(criteriaBuilder).like(lowerUsername, "%alice%");
    }
}
