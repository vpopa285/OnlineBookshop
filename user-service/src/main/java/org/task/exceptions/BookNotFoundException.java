package org.task.exceptions;

public class BookNotFoundException extends NotFoundException {
    public BookNotFoundException(Long id) {
        super("Book with id " + id + " does not exist");
    }
}
