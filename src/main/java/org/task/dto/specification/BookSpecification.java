package org.task.dto.specification;

import io.micrometer.common.util.StringUtils;
import lombok.NoArgsConstructor;

import org.springframework.data.jpa.domain.Specification;
import org.task.dto.filter.BookFilter;
import org.task.model.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
public class BookSpecification {

    public static Specification<Book> withFilter(BookFilter filter) {
        List<Specification<Book>> specifications = new ArrayList<>();

        if (StringUtils.isNotBlank(filter.getTitle())) {
            specifications.add(
                    titleContains(filter.getTitle())
            );
        }

        if (StringUtils.isNotBlank(filter.getAuthor())) {
            specifications.add(
                    authorContains(filter.getAuthor())
            );
        }

        if (filter.getGenres() != null && !filter.getGenres().isEmpty()) {
            specifications.add(
                    genreContains(filter.getGenres())
            );
        }

        if (filter.getPriceMax() != null) {
            specifications.add(
                    priceMax(filter.getPriceMax())
            );
        }

        if (filter.getPriceMin() != null) {
            specifications.add(
                    priceMin(filter.getPriceMin())
            );
        }

        if (filter.getPriceEqual() != null) {
            specifications.add(
                    priceEqual(filter.getPriceEqual())
            );
        }

        return specifications.stream()
                .reduce(Specification::and)
                .orElseGet(BookSpecification::empty);
    }

    private static Specification<Book> empty() {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.conjunction();
    }

    private static Specification<Book> titleContains(String title) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                root.get("title")
                        ),
                        "%" + title.toLowerCase() + "%"
                );
    }

    private static Specification<Book> authorContains(String author) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.like(
                        criteriaBuilder.lower(
                                root.get("author")
                        ),
                        "%" + author.toLowerCase() + "%"
                );
    }

    private static Specification<Book> genreContains(Set<String> genres) {
        return (root, query, criteriaBuilder) ->
                root.get("genre").in(genres);
    }

    private static Specification<Book> priceMax(
            Double price
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"),
                        price
                );
    }

    private static Specification<Book> priceMin(
            Double price
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"),
                        price
                );
    }

    private static Specification<Book> priceEqual(
            Double price
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.<Double>get("price"),
                        price
                );
    }
}
