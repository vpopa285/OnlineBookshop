package org.task;

import org.task.model.Book;

public enum SearchType {
    TITLE,
    AUTHOR,
    GENRE;

    public String extract(Book book) {
        return switch (this) {
            case TITLE -> "title";
            case AUTHOR -> "author";
            case GENRE -> "genre";
        };
    }
}
