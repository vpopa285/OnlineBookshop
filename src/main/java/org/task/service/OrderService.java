package org.task.service;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.task.dto.OrderRequest;
import org.task.dto.OrderResponse;
import org.task.exceptions.BookNotFoundException;
import org.task.exceptions.OrderNotFoundException;
import org.task.exceptions.UserNotFoundException;
import org.task.model.Book;
import org.task.model.Order;
import org.task.model.OrderItem;
import org.task.model.User;
import org.task.repositories.BookRepository;
import org.task.repositories.OrderItemRepository;
import org.task.repositories.OrderRepository;
import org.task.repositories.UserRepository;

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

    public OrderResponse findResponseById(Long id) {
        return orderRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(id)
        );
    }

    public List<OrderResponse> findResponsesByUserId(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        return orderRepository.findAllByUserId(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public OrderResponse create(OrderRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));
        Book book = bookRepository.findById(request.bookId())
                .orElseThrow(() -> new BookNotFoundException(request.bookId()));

        Order order = orderRepository.save(new Order(user));
        orderItemRepository.save(new OrderItem(order, book));

        return toResponse(order);
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
