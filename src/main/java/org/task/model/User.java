package org.task.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;
    private String username;
    private String email;
    private String password;
    @Setter
    private double amount;
    @Setter
    private boolean restriction;
    private boolean isAdmin;
}
