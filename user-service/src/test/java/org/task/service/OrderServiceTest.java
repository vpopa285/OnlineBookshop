package org.task.service;

import org.junit.jupiter.api.Test;
import org.task.client.BookClient;
import org.task.dto.request.OrderRequest;
import org.task.dto.response.BookResponse;
import org.task.exceptions.BookNotFoundException;
import org.task.exceptions.OrderNotFoundException;
import org.task.model.Order;
import org.task.model.OrderItem;
import org.task.model.User;
import org.task.repositories.OrderItemRepository;
import org.task.repositories.OrderRepository;
import org.task.repositories.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final BookClient bookClient = mock(BookClient.class);
    private final OrderService orderService = new OrderService(
            orderRepository,
            orderItemRepository,
            userRepository,
            bookClient
    );

    @Test
    void shouldCreateOrderWhenUserExistsAndBookExistsByHttp() {
        User user = User.builder().id(1L).username("user").build();
        Order order = new Order(user);
        order.setId(7L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookClient.findById(2L)).thenReturn(Optional.of(new BookResponse(
                2L,
                "Book",
                "Author",
                "Genre",
                "content",
                10
        )));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderItemRepository.findAllByOrder(order))
                .thenReturn(List.of(new OrderItem(order, 2L)));

        var response = orderService.create(new OrderRequest(1L, 2L));

        assertThat(response.user().id()).isEqualTo(1L);
        assertThat(response.books()).extracting(BookResponse::id).containsExactly(2L);
    }

    @Test
    void shouldReturnOrderWhenItBelongsToUser() {
        User user = User.builder().id(1L).username("user").build();
        Order order = new Order(user);
        order.setId(7L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findAllByOrder(order))
                .thenReturn(List.of(new OrderItem(order, 2L)));
        when(bookClient.findById(2L)).thenReturn(Optional.of(new BookResponse(
                2L,
                "Book",
                "Author",
                "Genre",
                "content",
                10
        )));

        var response = orderService.findResponseByUserIdAndOrderId(1L, 7L);

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.user().id()).isEqualTo(1L);
    }

    @Test
    void shouldNotReturnOrderWhenItDoesNotBelongToUser() {
        User pathUser = User.builder().id(1L).username("path-user").build();
        User orderUser = User.builder().id(2L).username("order-user").build();
        Order order = new Order(orderUser);
        order.setId(7L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(pathUser));
        when(orderRepository.findById(7L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.findResponseByUserIdAndOrderId(1L, 7L))
                .isInstanceOf(OrderNotFoundException.class);
    }

    @Test
    void shouldNotCreateOrderWhenBookDoesNotExistByHttp() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(User.builder().id(1L).build()));
        when(bookClient.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.create(new OrderRequest(1L, 2L)))
                .isInstanceOf(BookNotFoundException.class);
        verify(orderRepository, never()).save(any(Order.class));
    }
}
