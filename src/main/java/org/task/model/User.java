package org.task.model;

import lombok.Getter;
import lombok.Setter;
import org.task.Library;
import org.task.SearchType;

import java.util.HashSet;
import java.util.Set;

@Getter
public class User {
    protected final long id;
    private final String username;
    private final String email;
    private final String password;
    private double amount;
    @Setter
    private boolean restriction = false;

    private final Set<Book> purchasedBooks;

    private static long COUNTER = 0;

    public User(long id, String username, String email, String password, double amount, boolean restriction) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.amount = amount;
        this.restriction = restriction;

        purchasedBooks = new HashSet<>();
    }

    public User(String username, String email, String password) {
        this(COUNTER++, username, email, password, 0, false);
    }

    public User(String username, String email, String password, double amount) {
        this(COUNTER++, username, email, password, amount, false);
    }

    public void addFunds(double value) {
        amount += value;
    }

    public void buy(Book book) {
        if (amount >= book.getPrice()) {
            amount -= book.getPrice();
            purchasedBooks.add(book);

            System.out.println("Book purchased: " + book.getTitle());
        } else {
            System.out.println("Not enough money");
        }
    }

    public void review(Book book, int rate, String comment) {
        if (!restriction && purchasedBooks.contains(book)) {
            book.getReviews().add(new Review(rate, comment, this));
        } else {
            System.out.println("You are not allowed to left a review!");
        }
    }

    public Set<Book> searchBook(Library library, SearchType type, String value) {
        return library.search(type, value);
    }

}
