package org.task.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "reviews")
public class Review{
        @Setter
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "user_id")
        private Long userId;

        @Setter
        @ManyToOne
        @JoinColumn(name = "book_id")
        private Book book;

        @Column(name = "rating")
        private int rate;
        private String comment;
}
