package org.task.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.task.model.Order;
import org.task.model.OrderItem;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findAllByOrder(Order order);

    void deleteAllByOrderIn(List<Order> orders);
}
