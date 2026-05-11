package org.task;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Book {
    private final long id;
    private final String title;
    private final String author;
    private final String genre;
    private final String content;
    private final double price;

    private final List<Review> reviews;

    private static long COUNTER = 0;

    public Book(long id, String title, String author, String genre, String content, double price) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.content = content;
        this.price = price;

        reviews = new ArrayList<>();
    }

    public Book(String title, String author, String genre, String content, double price) {
        this(COUNTER++, title, author, genre, content, price);
    }

    public String viewBook() {
        return title +
                "\nby: " + author +
                "\n\n" + content + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book)) return false;
        return id == ((Book) o).id;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }

}
