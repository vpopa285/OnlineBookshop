package org.task.service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.task.dto.PageResponse;
import org.task.dto.filter.UserFilter;
import org.task.dto.request.AmountUpdateRequest;
import org.task.dto.request.UserRequest;
import org.task.dto.response.BookReadResponse;
import org.task.dto.response.UserResponse;
import org.task.dto.specification.UserSpecification;
import org.task.exceptions.BookNotFoundException;
import org.task.exceptions.UserNotFoundException;
import org.task.model.Book;
import org.task.model.Order;
import org.task.model.OrderItem;
import org.task.model.Review;
import org.task.model.User;
import org.task.repositories.BookRepository;
import org.task.repositories.OrderItemRepository;
import org.task.repositories.OrderRepository;
import org.task.repositories.ReviewRepository;
import org.task.repositories.UserRepository;

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

    public UserResponse findResponseById(Long id) {
        return userRepository.findById(id)
                .map(UserService::userToResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public PageResponse<UserResponse> findAllResponses(UserFilter filter, Pageable pageable) {
        Specification<User> specification = UserSpecification.withFilter(filter);

        Page<UserResponse> page = userRepository.findAll(specification, pageable)
                .map(UserService::userToResponse);

        return PageResponse.from(page);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void update(User user) {
        userRepository.save(user);
    }

    public UserResponse update(Long id, UserRequest request) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        User user = new User(
                id,
                request.username(),
                request.email(),
                request.password(),
                existingUser.getAmount(),
                existingUser.isRestriction(),
                existingUser.isAdmin()
        );

        return userToResponse(userRepository.save(user));
    }

    public UserResponse update(Long id, AmountUpdateRequest request) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setAmount(request.price());
                    return userToResponse(userRepository.save(user));
                })
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public void deleteById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.delete(user);
    }

    public BookReadResponse findReadableBook(Long userId, Long bookId) {
        bookRepository.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

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
                ))
                .orElseThrow(() -> new UserNotFoundException(userId));
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

    public static UserResponse userToResponse(User user) {
        return new UserResponse(
                user.getUsername(),
                user.getEmail(),
                user.isRestriction()
        );
    }

}
