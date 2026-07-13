package org.task.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.task.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
