package org.task.exceptions;

public class ReviewNotFoundException extends NotFoundException {
    public ReviewNotFoundException(Long id) {
        super("Review with id " + id + " does not exist");
    }
}
