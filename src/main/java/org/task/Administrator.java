package org.task;

public class Administrator extends User{

    public Administrator(String username, String email, String password) {
        super(username, email, password);
    }

    public void addBook(Library library, Book book) {
        library.getLibrary().add(book);
    }

    public void removeBook(Library library, long id) {
        Book book = findBook(library, id);
        if (book != null) {
            library.getLibrary().remove(book);
        }
    }

    public Book findBook(Library library, long id) {
        return library.findById(id).orElse(null);
    }

    public void setRestrictions(User user) {
        user.setRestriction(true);
    }

    public String seeStatistics(Library library) {
        return "Total books: " + library.getLibrary().size();
    }

}
