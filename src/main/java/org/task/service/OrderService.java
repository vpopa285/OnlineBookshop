package org.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.task.dto.OrderRequest;
import org.task.dto.OrderResponse;
import org.task.model.Book;
import org.task.model.Order;
import org.task.model.OrderItem;
import org.task.model.User;
import org.task.repositories.BookRepository;
import org.task.repositories.OrderItemRepository;
import org.task.repositories.OrderRepository;
import org.task.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    public List<OrderResponse> findAllResponses() {
        return orderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<OrderResponse> findResponseById(Long id) {
        return orderRepository.findById(id)
                .map(this::toResponse);
    }

    public List<OrderResponse> findResponsesByUserId(Long userId) {
        return orderRepository.findAllByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<OrderResponse> create(OrderRequest request) {
        Optional<User> user = userRepository.findById(request.userId());
        Optional<Book> book = bookRepository.findById(request.bookId());
        if (user.isEmpty() || book.isEmpty()) {
            return Optional.empty();
        }

        Order order = orderRepository.save(new Order(user.get()));
        orderItemRepository.save(new OrderItem(order, book.get()));

        return Optional.of(toResponse(order));
    }

    private OrderResponse toResponse(Order order) {
        List<Long> bookIds = orderItemRepository.findAllByOrder(order)
                .stream()
                .map(orderItem -> orderItem.getBook().getId())
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                bookIds
        );
    }
}
