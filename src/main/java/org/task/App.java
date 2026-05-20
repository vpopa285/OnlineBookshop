package org.task;

import org.task.model.Book;
import org.task.model.User;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class App {

    private static Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

    private static final Library library = new Library();
    private static final User user = new User("user", "ex@test.com", "1234", 50);
    private static final List<User> users = new ArrayList<>();

    private static final Administrator admin = new Administrator("admin", "admin@test.com", "admin");

    public static void main(String[] args) {
        init();
        runMenu();
    }

    static void init() {
        admin.addBook(library, new Book("Java", "James", "Programming", "Full Java content...", 20));
        admin.addBook(library, new Book("Python", "Guido", "Programming", "Python content...", 15));

        users.add(new User("mark", "mail@test.com", "1234"));
        users.add(new User("ann", "mail@test.com", "1234"));
    }

    static void initScanner() {
        scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    }

    static void runMenu() {
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

    private static void previewBook(Book book) {
        System.out.println("\nTitle: " + book.getTitle());
        System.out.println("Author: " + book.getAuthor());
        System.out.println("Genre: " + book.getGenre());

        System.out.println("\nReviews:");
        if (book.getReviews().isEmpty()) {
            System.out.println("No reviews yet");
        } else {
            book.getReviews().forEach(r ->
                    System.out.println("- " + r.user().getUsername()
                            + " (" + r.rate() + "): " + r.comment())
            );
        }
    }

    private static void search() {
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

        Set<Book> result = user.searchBook(library, type, value);

        if (result.isEmpty()) {
            System.out.println("No books found");
            return;
        }

        result.forEach(b -> System.out.println(b.getId() + " - " + b.getTitle()));

        System.out.println("\nEnter book id to preview (or -1 to exit):");
        long id = readLong();

        if (id == -1) return;

        result.stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .ifPresentOrElse(
                        App::previewBook,
                        () -> System.out.println("Invalid book id")
                );
    }

    private static void buy() {
        System.out.println("Book id:");
        long id = readLong();

        library.findById(id)
                .ifPresentOrElse(
                        user::buy,
                        () -> System.out.println("Book not found")
                );
    }

    private static void showPurchased() {
        if (user.getPurchasedBooks().isEmpty()) {
            System.out.println("No purchased books");
            return;
        }

        user.getPurchasedBooks().forEach(b -> System.out.println(b.getTitle()));
    }

    private static void readBook() {
        System.out.println("Book id:");
        long id = readLong();

        user.getPurchasedBooks().stream()
                .filter(b -> b.getId() == id)
                .findFirst()
                .ifPresentOrElse(
                        b -> System.out.println(b.viewBook()),
                        () -> System.out.println("You don't own this book")
                );
    }

    private static void review() {
        System.out.println("Book id:");
        long id = readLong();

        Book book = user.getPurchasedBooks().stream()
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

        user.review(book, rate, comment);
    }

    private static void addMoney() {
        System.out.println("Amount:");
        double amount = readDouble();

        if (amount <= 0) {
            System.out.println("Invalid amount");
            return;
        }

        user.addFunds(amount);
    }

    private static void adminMenu() {
        System.out.println("""
                1. Add book
                2. Remove book
                3. Restrict user
                4. See statistics
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

                admin.addBook(library, new Book(title, author, genre, content, price));
            }
            case "2" -> {
                System.out.println("Book id:");
                long id = readLong();

                admin.removeBook(library, id);
            }
            case "3" -> {
                System.out.println("Users:");
                users.forEach(u -> System.out.println(u.getId() + " - " + u.getUsername()));

                System.out.println("User id:");
                long id = readLong();

                users.stream()
                        .filter(u -> u.getId() == id)
                        .findFirst()
                        .ifPresentOrElse(
                                admin::setRestrictions,
                                () -> System.out.println("User not found")
                        );
            }
            case "4" -> System.out.println(admin.seeStatistics(library));
            default -> System.out.println("Invalid option");
        }
    }

    private static long readLong() {
        try {
            return Long.parseLong(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid number");
            return -1;
        }
    }

    private static int readInt() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid number");
            return -1;
        }
    }

    private static double readDouble() {
        try {
            return Double.parseDouble(scanner.nextLine());
        } catch (Exception e) {
            System.out.println("Invalid number");
            return -1;
        }
    }

}
