package org.task.service;

import org.junit.jupiter.api.Test;
import org.task.client.BookClient;
import org.task.client.ReviewClient;
import org.task.dto.request.UserRequest;
import org.task.dto.response.BookResponse;
import org.task.exceptions.BookNotFoundException;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {
    private final UserRepository userRepository = mock(UserRepository.class);
    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
    private final BookClient bookClient = mock(BookClient.class);
    private final ReviewClient reviewClient = mock(ReviewClient.class);
    private final UserService userService = new UserService(
            userRepository,
            orderRepository,
            orderItemRepository,
            bookClient,
            reviewClient
    );

    @Test
    void shouldCreateUserResponse() {
        User savedUser = User.builder()
                .id(1L)
                .username("reader")
                .email("reader@example.com")
                .restriction(false)
                .build();

        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenReturn(savedUser);

        var response = userService.createResponse(new UserRequest(
                "reader",
                "reader@example.com",
                "password123"
        ));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("reader");
    }

    @Test
    void shouldUpdateUserWhenUserExists() {
        User existingUser = User.builder()
                .id(1L)
                .username("reader")
                .email("reader@example.com")
                .password("password123")
                .amount(25)
                .restriction(false)
                .isAdmin(false)
                .build();
        User updatedUser = new User(
                1L,
                "updated_reader",
                "updated@example.com",
                "password123",
                25,
                false,
                false
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(org.mockito.ArgumentMatchers.any(User.class)))
                .thenReturn(updatedUser);

        var response = userService.update(1L, new UserRequest(
                "updated_reader",
                "updated@example.com",
                "password123"
        ));

        assertThat(response.username()).isEqualTo("updated_reader");
        assertThat(response.email()).isEqualTo("updated@example.com");
    }

    @Test
    void shouldDeleteUserAndRemoteReviews() {
        User user = User.builder().id(1L).username("reader").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(orderRepository.findAllByUserId(1L)).thenReturn(List.of());

        userService.deleteById(1L);

        verify(reviewClient).deleteByUserId(1L);
        verify(userRepository).delete(user);
    }

    @Test
    void shouldReturnReadableBookWhenBookWasPurchased() {
        User user = User.builder().id(1L).username("reader").build();
        Order order = new Order(user);
        BookResponse book = new BookResponse(
                2L,
                "Clean Code",
                "Robert C. Martin",
                "Programming",
                "Practical programming advice.",
                29.99
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookClient.findById(2L)).thenReturn(Optional.of(book));
        when(orderRepository.findAllByUserId(1L)).thenReturn(List.of(order));
        when(orderItemRepository.findAllByOrder(order))
                .thenReturn(List.of(new OrderItem(order, 2L)));

        var response = userService.findReadableBook(1L, 2L);

        assertThat(response.id()).isEqualTo(2L);
        assertThat(response.content()).isEqualTo("Practical programming advice.");
    }

    @Test
    void shouldNotReturnReadableBookWhenBookWasNotPurchased() {
        User user = User.builder().id(1L).username("reader").build();
        BookResponse book = new BookResponse(
                2L,
                "Clean Code",
                "Robert C. Martin",
                "Programming",
                "Practical programming advice.",
                29.99
        );

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookClient.findById(2L)).thenReturn(Optional.of(book));
        when(orderRepository.findAllByUserId(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> userService.findReadableBook(1L, 2L))
                .isInstanceOf(BookNotFoundException.class);
    }
}
