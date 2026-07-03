package org.task.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.task.BookshopApplication;
import org.task.model.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BookshopApplication.class)
@ActiveProfiles("test")
@Sql(scripts = "/db/initSchema.sql")
public class UserServiceTest {
    @Autowired
    UserService userService;

    User user = User.builder()
            .username("u")
            .email("e")
            .password("p")
            .build();

    @Test
    void shouldCreateAndFindUser() {
        userService.create(user);

        User found = userService.findById(user.getId()).get();

        assertThat(found.getId()).isEqualTo(user.getId());
    }

    @Test
    @DirtiesContext
    void shouldFindAllUsers() {
        userService.create(user);

        List<User> foundUsers = userService.findAll();

        assertThat(foundUsers.size()).isEqualTo(1);
    }

    @Test
    @DirtiesContext
    void shouldUpdateBook() {
        userService.create(user);

        User updated = new User(user.getId(), "Update", "LM", "IT", 0, false, false);
        userService.update(updated);

        User found = userService.findById(user.getId()).get();

        assertThat(updated.getUsername()).isEqualTo(found.getUsername());
    }

    @Test
    @DirtiesContext
    void shouldDeleteBook() {
        userService.create(user);

        userService.deleteById(user.getId());

        Optional<User> extract = userService.findById(user.getId());

        assertThat(extract).isEmpty();
    }
}
