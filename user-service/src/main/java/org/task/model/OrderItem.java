package org.task.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne
    @NonNull private Order order;

    @Column(name = "book_id")
    @NonNull private Long bookId;
}
