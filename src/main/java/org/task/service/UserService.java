package org.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.task.dto.AmountUpdateRequest;
import org.task.dto.BookReadResponse;
import org.task.dto.UserRequest;
import org.task.dto.UserResponse;
import org.task.model.*;
import org.task.repositories.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;

    public void create(User user) {
        userRepository.save(user);
    }

    public UserResponse createResponse(UserRequest request) {
        return userToResponse(userRepository.save(requestToUser(request)));
    }

    public Optional<User> findById(long id) {
        return userRepository.findById(id);
    }

    public Optional<UserResponse> findResponseById(Long id) {
        return userRepository.findById(id)
                .map(this::userToResponse);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<UserResponse> findAllResponses() {
        return userRepository.findAll()
                .stream()
                .map(this::userToResponse)
                .toList();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void update(User user) {
        userRepository.save(user);
    }

    public UserResponse update(Long id, UserRequest request) {
        User user = requestToUser(request);
        user.setId(id);

        return userToResponse(userRepository.save(user));
    }

    public Optional<UserResponse> update(Long id, AmountUpdateRequest request) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setAmount(request.price());
                    return userToResponse(userRepository.save(user));
                });
    }

    public void deleteById(long id) {
        userRepository.deleteById(id);
    }

    public Optional<BookReadResponse> findReadableBook(Long userId, Long bookId) {
        return userRepository.findById(userId)
                .flatMap(user -> getPurchasedBooks(user)
                        .stream()
                        .filter(book -> book.getId().equals(bookId))
                        .findFirst())
                .map(book -> new BookReadResponse(
                        book.getId(),
                        book.getTitle(),
                        book.getAuthor(),
                        book.getGenre(),
                        book.getContent()
                ));
    }

    @Transactional
    public void resetUserActivity(long userId) {
        List<Order> orders = orderRepository.findAllByUserId(userId);
        if (!orders.isEmpty()) {
            orderItemRepository.deleteAllByOrderIn(orders);
            orderRepository.deleteAll(orders);
        }
        reviewRepository.deleteAllByUserId(userId);
    }

    public boolean buy(User user, long bookId) {
        Optional<Book> bookOpt = bookRepository.findById(bookId);

        if (bookOpt.isEmpty()) {
            return false;
        }
        Book book = bookOpt.get();

        if (user.getAmount() < book.getPrice()) {
            return false;
        }

        user.setAmount(user.getAmount() - book.getPrice());

        Order order = new Order(user);
        orderRepository.save(order);

        OrderItem orderItem = new OrderItem(order, book);
        orderItemRepository.save(orderItem);

        return true;
    }

    public Set<Book> getPurchasedBooks(User user) {
        List<Order> orders = orderRepository.findAllByUserId(user.getId());

        Set<Book> books = new HashSet<>();

        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findAllByOrder(order);

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
            reviewRepository.save(Review.builder()
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
        userRepository.save(user);
    }

    public boolean setRestrictionsByUserId(User admin, long id) {
        Optional<User> user = userRepository.findById(id);
        if(!admin.isAdmin() || user.isEmpty()) {
            return false;
        }

        user.get().setRestriction(true);
        userRepository.save(user.get());

        return true;
    }

    public User requestToUser(UserRequest request) {
        return User.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .amount(0)
                .restriction(false)
                .isAdmin(false)
                .build();
    }

    private UserResponse userToResponse(User user) {
        return new UserResponse(
                user.getUsername(),
                user.getEmail(),
                user.isRestriction()
        );
    }

}
