package org.task;

import lombok.Getter;


import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Library {
    @Getter
    private final Set<Book> library = new HashSet<>();

    public Set<Book> search(SearchType type, String value) {
        return library.stream()
                .filter(book -> type.extract(book).equalsIgnoreCase(value))
                .collect(Collectors.toSet());
    }

    public Optional<Book> findById(long id) {
        return library.stream()
                .filter(b -> b.getId() == id)
                .findFirst();
    }

}
