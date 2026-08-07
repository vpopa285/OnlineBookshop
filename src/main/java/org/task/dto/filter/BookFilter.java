package org.task.dto.filter;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class BookFilter {
    private String title;
    private String author;
    private Set<String> genres;
    private Double priceMin;
    private Double priceMax;
    private Double priceEqual;
}
