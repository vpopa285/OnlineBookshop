package org.task.dto.specification;

import io.micrometer.common.util.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.task.dto.filter.UserFilter;
import org.task.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {
    public static Specification<User> withFilter(UserFilter filter) {
        List<Specification<User>> specifications = new ArrayList<>();

        if (StringUtils.isNotBlank(filter.getUsername())) {
            specifications.add(userNameContains(filter.getUsername()));
        }

        if (StringUtils.isNotBlank(filter.getEmail())) {
            specifications.add(emailContains(filter.getEmail()));
        }

        return specifications.stream()
                .reduce(Specification::and)
                .orElseGet(UserSpecification::empty);
    }

    private static Specification<User> empty() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.conjunction();
    }

    private static Specification<User> userNameContains(String username) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("username")),
                        "%" + username.toLowerCase() + "%"
                );
    }

    private static Specification<User> emailContains(String email) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + email.toLowerCase() + "%"
                );
    }
}
