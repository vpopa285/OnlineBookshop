package org.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.task.dao.*;
import org.task.model.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDao userDao;
    private final BookDao bookDao;
    private final ReviewDao reviewDao;
    private final OrderDao orderDao;
    private final OrderItemDao orderItemDao;

    public void create(User user) {
        userDao.create(user);
    }

    public Optional<User> findById(long id) {
        return userDao.findById(id);
    }

    public List<User> findAll() {
        return userDao.findAll();
    }

    public void update(User user) {
        userDao.update(user);
    }

    public void deleteById(long id) {
        userDao.deleteById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    public void resetUserActivity(long userId) {
        userDao.resetUserActivity(userId);
    }

    public boolean buy(User user, long bookId) {
        Book book = bookDao.findById(bookId).orElse(null);

        if (book == null) {
            return false;
        }

        if (user.getAmount() < book.getPrice()) {
            return false;
        }

        user.setAmount(user.getAmount() - book.getPrice());
        userDao.update(user);

        Order order = Order.builder()
                .user(user)
                .build();

        orderDao.create(order);

        OrderItem orderItem = OrderItem.builder()
                .order(order)
                .book(book)
                .build();

        orderItemDao.create(orderItem);

        return true;
    }

    public Set<Book> getPurchasedBooks(User user) {
        List<Order> orders = orderDao.findAllByUserId(user.getId());

        Set<Book> books = new HashSet<>();

        for (Order order : orders) {
            List<OrderItem> items = orderItemDao.findAllByOrderId(order.getId());

            for (OrderItem item : items) {
                books.add(item.getBook());
            }
        }

        return books;
    }

    public void reviewBook(User user, Book book, int rate, String comment) {
        boolean purchased = getPurchasedBooks(user).stream()
                .anyMatch(purchasedBook -> purchasedBook.getId().equals(book.getId()));

        if (!user.isRestriction() && purchased) {
            reviewDao.create(book.getId(), Review.builder()
                    .user(user)
                    .book(book)
                    .rate(rate)
                    .comment(comment)
                    .build()
            );
        }
    }

    public void addFunds(User user, double amount) {
        user.setAmount(user.getAmount() + amount);
        userDao.update(user);
    }

    public boolean setRestrictionsByUserId(User admin, long id) {
        User user = userDao.findById(id).orElse(null);
        if(!admin.isAdmin() || user == null) {
            return false;
        }

        user.setRestriction(true);
        userDao.update(user);

        return true;
    }
}
