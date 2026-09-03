package org.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.task.client.BookClient;
import org.task.client.ReviewClient;
import org.task.dto.PageResponse;
import org.task.dto.filter.UserFilter;
import org.task.dto.request.AmountUpdateRequest;
import org.task.dto.request.UserRequest;
import org.task.dto.request.UserReviewRequest;
import org.task.dto.response.BookReadResponse;
import org.task.dto.response.BookResponse;
import org.task.dto.response.UserResponse;
import org.task.dto.specification.UserSpecification;
import org.task.exceptions.BookNotFoundException;
import org.task.exceptions.UserNotFoundException;
import org.task.model.Order;
import org.task.model.OrderItem;
import org.task.model.User;
import org.task.repositories.OrderItemRepository;
import org.task.repositories.OrderRepository;
import org.task.repositories.UserRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BookClient bookClient;
    private final ReviewClient reviewClient;
    private final PasswordEncoder passwordEncoder;

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
                passwordEncoder.encode(request.password()),
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
        resetUserActivity(id);
        reviewClient.deleteByUserId(id);
        userRepository.delete(user);
    }

    public BookReadResponse findReadableBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        BookResponse book = bookClient.findById(bookId)
                .orElseThrow(() -> new BookNotFoundException(bookId));

        if (!getPurchasedBookIds(user).contains(bookId)) {
            throw new BookNotFoundException(bookId);
        }

        return toBookReadResponse(book);
    }

    public List<BookReadResponse> findReadableBooks(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return getPurchasedBookIds(user)
                .stream()
                .map(bookId -> bookClient.findById(bookId)
                        .orElseThrow(() -> new BookNotFoundException(bookId)))
                .map(UserService::toBookReadResponse)
                .toList();
    }

    @Transactional
    public void resetUserActivity(long userId) {
        List<Order> orders = orderRepository.findAllByUserId(userId);
        if (!orders.isEmpty()) {
            orderItemRepository.deleteAllByOrderIn(orders);
            orderRepository.deleteAll(orders);
        }
    }

    public boolean buy(User user, long bookId) {
        Optional<BookResponse> bookOpt = bookClient.findById(bookId);

        if (bookOpt.isEmpty()) {
            return false;
        }
        BookResponse book = bookOpt.get();

        if (user.getAmount() < book.price()) {
            return false;
        }

        user.setAmount(user.getAmount() - book.price());

        Order order = new Order(user);
        orderRepository.save(order);

        OrderItem orderItem = new OrderItem(order, book.id());
        orderItemRepository.save(orderItem);

        return true;
    }

    public Set<Long> getPurchasedBookIds(User user) {
        List<Order> orders = orderRepository.findAllByUserId(user.getId());

        Set<Long> bookIds = new LinkedHashSet<>();

        for (Order order : orders) {
            List<OrderItem> items = orderItemRepository.findAllByOrder(order);

            for (OrderItem item : items) {
                bookIds.add(item.getBookId());
            }
        }

        return bookIds;
    }

    public void reviewBook(User user, Long bookId, int rate, String comment) {
        boolean purchased = getPurchasedBookIds(user).contains(bookId);

        if (!user.isRestriction() && purchased) {
            reviewClient.createForUser(user.getId(), new UserReviewRequest(bookId, rate, comment));
        }
    }

    public void addFunds(User user, double amount) {
        user.setAmount(user.getAmount() + amount);
        userRepository.save(user);
    }

    public boolean setRestrictionsByUserId(User admin, long id) {
        Optional<User> user = userRepository.findById(id);
        if (!admin.isAdmin() || user.isEmpty()) {
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
                .password(passwordEncoder.encode(request.password()))
                .amount(0)
                .restriction(false)
                .isAdmin(false)
                .build();
    }

    public static UserResponse userToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.isRestriction()
        );
    }

    private static BookReadResponse toBookReadResponse(BookResponse book) {
        return new BookReadResponse(
                book.id(),
                book.title(),
                book.author(),
                book.genre(),
                book.content()
        );
    }
}
