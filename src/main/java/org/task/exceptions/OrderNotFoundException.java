package org.task.exceptions;

public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(Long id) {
        super("Order with id " + id + " does not exist");
    }
}
