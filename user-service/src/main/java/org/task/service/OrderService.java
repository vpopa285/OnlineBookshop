package org.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.task.client.BookClient;
import org.task.dto.PageResponse;
import org.task.dto.filter.OrderFilter;
import org.task.dto.request.OrderRequest;
import org.task.dto.response.BookResponse;
import org.task.dto.response.OrderResponse;
import org.task.dto.specification.OrderSpecification;
import org.task.exceptions.BookNotFoundException;
import org.task.exceptions.OrderNotFoundException;
import org.task.exceptions.UserNotFoundException;
import org.task.model.Order;
import org.task.model.OrderItem;
import org.task.model.User;
import org.task.repositories.OrderItemRepository;
import org.task.repositories.OrderRepository;
import org.task.repositories.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
    private final BookClient bookClient;

    public PageResponse<OrderResponse> findAllResponses(OrderFilter filter, Pageable pageable) {
        Specification<Order> specification = OrderSpecification.getSpecification(filter);

        Page<OrderResponse> page = orderRepository.findAll(specification, pageable)
                .map(this::toResponse);

        return PageResponse.from(page);
    }

    public OrderResponse findResponseById(Long id) {
        return orderRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    public OrderResponse findResponseByUserIdAndOrderId(Long userId, Long orderId) {
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        if (!order.getUser().getId().equals(userId)) {
            throw new OrderNotFoundException(orderId);
        }

        return toResponse(order);
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

        BookResponse book = bookClient.findById(request.bookId())
                .orElseThrow(() -> new BookNotFoundException(request.bookId()));

        Order order = orderRepository.save(new Order(user));
        orderItemRepository.save(new OrderItem(order, book.id()));

        return toResponse(order);
    }

    public OrderResponse createForUser(Long userId, Long bookId) {
        return create(new OrderRequest(userId, bookId));
    }

    private OrderResponse toResponse(Order order) {
        List<BookResponse> books = orderItemRepository.findAllByOrder(order)
                .stream()
                .map(OrderItem::getBookId)
                .map(bookId -> bookClient.findById(bookId)
                        .orElseThrow(() -> new BookNotFoundException(bookId)))
                .toList();

        return new OrderResponse(
                order.getId(),
                UserService.userToResponse(order.getUser()),
                books,
                order.getCreatedAt()
        );
    }
}
