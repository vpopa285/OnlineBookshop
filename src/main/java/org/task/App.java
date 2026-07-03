package org.task;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.task.model.Book;
import org.task.model.User;
import org.task.service.BookService;
import org.task.service.ReviewService;
import org.task.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
@RequiredArgsConstructor
public class App {

    private static Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

    @Getter
    private final BookService bookService;
    @Getter
    private final UserService userService;
    @Getter
    private final ReviewService reviewService;

    public void run() {
        init();
        runMenu();
    }

    private User user;
    private User admin;

    public void init() {
        user = userService.findByUsername("user").orElseGet(() -> {
            User newUser = new User(null, "user", "ex@test.com", "1234", 15, false, false);
            userService.create(newUser);
            return newUser;
        });
        userService.resetUserActivity(user.getId());
        user.setAmount(15);
        user.setRestriction(false);
        userService.update(user);

        admin = userService.findByUsername("admin").orElseGet(() -> {
            User newAdmin = new User(null, "admin", "admin@test.com", "admin", 100, false, true);
            userService.create(newAdmin);
            return newAdmin;
        });

        seedBook("Python", "James", "Programming", "Python content...", 10);
        seedBook("Java", "James", "Programming", "Java content...", 20);
    }

    private void seedBook(String title, String author, String genre, String content, double price) {
        if (!bookService.existsByTitle(title)) {
            bookService.create(Book.builder()
                    .title(title)
                    .author(author)
                    .genre(genre)
                    .content(content)
                    .price(price)
                    .build());
        }
    }

    void initScanner() {
        scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    }

    void runMenu() {
        boolean running = true;
        while (running) {
            System.out.println("""
                    1. Search books
                    2. Buy book
                    3. View purchased books
                    4. Read book
                    5. Add review
                    6. Add money
                    7. Admin panel
                    0. Exit
                    """);

            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> search();
                case "2" -> buy();
                case "3" -> showPurchased();
                case "4" -> readBook();
                case "5" -> review();
                case "6" -> addMoney();
                case "7" -> adminMenu();
                case "0" -> running = false;
                default -> System.out.println("Invalid option");
            }
        }
    }

    private void previewBook(Book book) {
        System.out.println("\nTitle: " + book.getTitle());
        System.out.println("Author: " + book.getAuthor());
        System.out.println("Genre: " + book.getGenre());

        System.out.println("\nReviews:");
        if (book.getReviews().isEmpty()) {
            System.out.println("No reviews yet");
        } else {
            book.getReviews().forEach(r ->
                    System.out.println("- " + r.getUser().getUsername()
                            + " (" + r.getRate() + "): " + r.getComment())
            );
        }
    }

    private void search() {
        System.out.println("Search by: TITLE / AUTHOR / GENRE");

        SearchType type;
        try {
            type = SearchType.valueOf(scanner.nextLine().toUpperCase());
        } catch (Exception e) {
            System.out.println("Invalid search type");
            return;
        }

        System.out.println("Value:");
        String value = scanner.nextLine();

        List<Book> result = bookService.findAllBooksByParam(type, value);

        if (result.isEmpty()) {
            System.out.println("No books found");
            return;
        }

        result.forEach(b -> System.out.println(b.getId() + " - " + b.getTitle()));

        System.out.println("\nEnter book id to preview (or -1 to exit):");
        long id = readLong();

        if (id == -1) return;

        bookService.findByIdWithReviews(id).ifPresentOrElse(
                this::previewBook,
                () -> System.out.println("Invalid book id")
        );
    }

    private void buy() {
        System.out.println("Book id:");
        long id = readLong();

        Optional<Book> book = bookService.findById(id);
        if (book.isEmpty()) {
            System.out.println("Book not found");
            return;
        }

        if (userService.getPurchasedBooks(user).stream().anyMatch(b -> b.getId().equals(id))) {
            System.out.println(book.get().getTitle());
            return;
        }

        if (user.getAmount() < book.get().getPrice()) {
            System.out.println("Not enough money");
            return;
        }

        if (userService.buy(user, id)) {
            System.out.println("Successfully purchased the book!");
        } else {
            System.out.println("Something went wrong!");
        }
    }

    private void showPurchased() {
        Set<Book> purchasedBooks = userService.getPurchasedBooks(user);
        if (purchasedBooks.isEmpty()) {
            System.out.println("You haven't purchased any books yet.");
        } else {
            System.out.println("Your purchased books:");
            purchasedBooks.forEach(b -> System.out.println(b.getId() + " - " + b.getTitle()));
        }
    }

    private void readBook() {
        System.out.println("Book id:");
        long id = readLong();

        userService.getPurchasedBooks(user).stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .ifPresentOrElse(
                        b -> System.out.println(
                                "\nTitle: " + b.getTitle() +
                                "\nAuthor: " + b.getAuthor() +
                                "\nGenre: " + b.getGenre() +
                                "\nContent: " + b.getContent()),
                        () -> System.out.println("You don't own this book")
                );
    }

    private void review() {
        if (user.isRestriction()) {
            System.out.println("You don't have rights to left a comment!");
            return;
        }

        System.out.println("Book id:");
        long id = readLong();

        Book book = userService.getPurchasedBooks(user).stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .orElse(null);

        if (book == null) {
            System.out.println("You don't own this book");
            return;
        }

        System.out.println("Rate (1-5):");
        int rate = readInt();

        if (rate < 1 || rate > 5) {
            System.out.println("Invalid rating");
            return;
        }

        System.out.println("Comment:");
        String comment = scanner.nextLine();

        userService.reviewBook(user, book, rate, comment);
    }

    private void addMoney() {
        System.out.println("Amount:");
        double amount = readDouble();

        if (amount <= 0) {
            System.out.println("Invalid amount");
            return;
        }

        userService.addFunds(user, amount);
    }

    private void adminMenu() {
        System.out.println("""
                1. Add book
                2. Remove book
                3. Restrict user
                4. Total books
                """);

        String choice = scanner.nextLine();

        switch (choice) {
            case "1" -> {
                System.out.println("Title:");
                String title = scanner.nextLine();

                System.out.println("Author:");
                String author = scanner.nextLine();

                System.out.println("Genre:");
                String genre = scanner.nextLine();

                System.out.println("Content:");
                String content = scanner.nextLine();

                System.out.println("Price:");
                double price = readDouble();

                bookService.create(Book.builder()
                        .title(title)
                        .author(author)
                        .genre(genre)
                        .content(content)
                        .price(price)
                        .build()
                );
            }
            case "2" -> {
                System.out.println("Book id:");
                long id = readLong();

                if (bookService.findById(id).isPresent()) {
                    bookService.deleteById(id);
                    System.out.println("Book removed successfully.");
                } else {
                    System.out.println("Book not found.");
                }
            }
            case "3" -> {
                System.out.println("Users:");
                userService.findAll().forEach(u -> System.out.println(u.getId() + " - " + u.getUsername()));

                System.out.println("User id:");
                long id = readLong();

                if (userService.findById(id).isEmpty()) {
                    System.out.println("User not found");
                    return;
                }

                if (userService.setRestrictionsByUserId(admin, id)) {
                    System.out.println("User restrictions updated.");
                } else {
                    System.out.println("Something went wrong!");
                }

            }
            case "4" -> System.out.println("Total books: " + bookService.count());
            default -> System.out.println("Invalid option");
        }
    }

    private long readLong() {
        try {
            return Long.parseLong(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid number");
            return -1;
        }
    }

    private int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid number");
            return -1;
        }
    }

    private double readDouble() {
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid number");
            return -1;
        }
    }

}
