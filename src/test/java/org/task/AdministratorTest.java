package org.task;

import org.junit.jupiter.api.Test;
import org.task.model.Book;
import org.task.model.User;

import static org.assertj.core.api.Assertions.assertThat;

class AdministratorTest {

    @Test
    void addBookToLibraryTest() {
        Administrator admin = new Administrator("admin", "a@mail.com", "123");
        Library library = new Library();
        Book book = new Book("Java", "James", "Programming", "content", 10);

        admin.addBook(library, book);

        assertThat(library.getLibrary()).hasSize(1).contains(book);
    }

    @Test
    void removeBookByIdTest() {
        Administrator admin = new Administrator("admin", "a@mail.com", "123");
        Library library = new Library();
        Book book = new Book("Java", "James", "Programming", "content", 10);

        admin.addBook(library, book);
        admin.removeBook(library, book.getId());

        assertThat(library.getLibrary()).isEmpty();
    }

    @Test
    void bookDoesNotExistTest() {
        Administrator admin = new Administrator("admin", "a@mail.com", "123");
        Library library = new Library();

        admin.removeBook(library, 999);

        assertThat(library.getLibrary()).isEmpty();
    }

    @Test
    void findBookByIdTest() {
        Administrator admin = new Administrator("admin", "a@mail.com", "123");
        Library library = new Library();
        Book book = new Book("Java", "James", "Programming", "content", 10);

        admin.addBook(library, book);

        Book found = admin.findBook(library, book.getId());

        assertThat(found).isEqualTo(book);
    }

    @Test
    void bookNotFoundTest() {
        Administrator admin = new Administrator("admin", "a@mail.com", "123");
        Library library = new Library();

        Book result = admin.findBook(library, 999);

        assertThat(result).isNull();
    }

    @Test
    void restrictUserTest() {
        Administrator admin = new Administrator("admin", "a@mail.com", "123");
        User user = new User("user", "u@mail.com", "123");

        admin.setRestrictions(user);

        assertThat(user.isRestriction());
    }

    @Test
    void showStatisticsTest() {
        Administrator admin = new Administrator("admin", "a@mail.com", "123");
        Library library = new Library();

        admin.addBook(library, new Book("Java", "James", "Programming", "content", 10));
        admin.addBook(library, new Book("Python", "Guido", "Programming", "content", 15));

        String stats = admin.seeStatistics(library);

        assertThat(stats).isEqualTo("Total books: 2");
    }

}
