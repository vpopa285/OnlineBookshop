package org.task;

import org.task.model.Book;

public enum SearchType {
    TITLE,
    AUTHOR,
    GENRE;

    public String extract(Book book) {
        return switch (this) {
            case TITLE -> book.getTitle();
            case AUTHOR -> book.getAuthor();
            case GENRE -> book.getGenre();
        };
    }
}
