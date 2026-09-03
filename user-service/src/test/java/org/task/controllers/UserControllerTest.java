package org.task.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.task.UserServiceApplication;
import org.task.client.BookClient;
import org.task.client.ReviewClient;
import org.task.dto.response.BookResponse;
import org.task.dto.response.ReviewResponse;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = UserServiceApplication.class)
@ActiveProfiles("test")
@Sql(scripts = "/db/init.sql")
class UserControllerTest {
    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private BookClient bookClient;

    @MockitoBean
    private ReviewClient reviewClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        initBooks();
        when(reviewClient.findByUserId(any())).thenReturn(List.of());
    }

    @Test
    void shouldRunUserCrudHappyPaths() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice_reads"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "new_reader",
                                  "email": "new_reader@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("new_reader"));

        mockMvc.perform(put("/users/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "updated_reader",
                                  "email": "updated_reader@example.com",
                                  "password": "new_password"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated_reader"));

        mockMvc.perform(patch("/users/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":75.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated_reader"));

        mockMvc.perform(delete("/users/4"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRunUserNestedHappyPaths() throws Exception {
        when(reviewClient.findByUserId(1L)).thenReturn(List.of(new ReviewResponse(
                1L,
                1L,
                1L,
                5,
                "Great."
        )));
        when(reviewClient.createForUser(any(), any())).thenReturn(Optional.of(new ReviewResponse(
                2L,
                1L,
                2L,
                4,
                "Strong read."
        )));

        mockMvc.perform(get("/users/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(post("/users/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": 2,
                                  "rate": 4,
                                  "comment": "Strong read."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.bookId").value(2));

        mockMvc.perform(get("/users/1/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/users/1/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("A Jazz Age story."));
    }

    @Test
    void shouldRunCurrentUserHappyPaths() throws Exception {
        when(reviewClient.findByUserId(1L)).thenReturn(List.of(new ReviewResponse(
                1L,
                1L,
                1L,
                5,
                "Great."
        )));
        when(reviewClient.createForUser(any(), any())).thenReturn(Optional.of(new ReviewResponse(
                2L,
                1L,
                2L,
                4,
                "Strong read."
        )));

        mockMvc.perform(get("/users/me").principal(jwtUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice_reads"));

        mockMvc.perform(get("/users/me/reviews").principal(jwtUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(post("/users/me/reviews")
                        .principal(jwtUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": 2,
                                  "rate": 4,
                                  "comment": "Strong read."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.bookId").value(2));

        mockMvc.perform(get("/users/me/orders").principal(jwtUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        mockMvc.perform(get("/users/me/orders/1").principal(jwtUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/users/me/orders/2").principal(jwtUser(1L)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Order with id 2 does not exist"));

        mockMvc.perform(post("/users/me/orders")
                        .principal(jwtUser(1L))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bookId\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.id").value(1))
                .andExpect(jsonPath("$.books[0].id").value(2));

        mockMvc.perform(get("/users/me/books").principal(jwtUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(get("/users/me/books/1").principal(jwtUser(1L)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("A Jazz Age story."));
    }

    @Test
    void shouldRunNestedUserBookAndOrderLookups() throws Exception {
        mockMvc.perform(get("/users/1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));

        mockMvc.perform(get("/users/1/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        mockMvc.perform(get("/users/1/orders/2"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Order with id 2 does not exist"));
    }

    @Test
    void shouldKeepApiUserPrefixWorking() throws Exception {
        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("alice_reads"));
    }

    @Test
    void shouldFilterAndSortUsers() throws Exception {
        mockMvc.perform(get("/users")
                        .param("username", "alice")
                        .param("email", "example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].username").value("alice_reads"));

        mockMvc.perform(get("/users").param("sort", "email,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("restricted@example.com"))
                .andExpect(jsonPath("$.sort[0]").value("email,desc"));
    }

    @Test
    void shouldReturnNotFoundWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User with id 999 does not exist"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingMissingUser() throws Exception {
        mockMvc.perform(put("/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "missing_reader",
                                  "email": "missing_reader@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User with id 999 does not exist"));
    }

    @Test
    void shouldReturnAllUserValidationErrors() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "",
                                  "email": "wrong-email",
                                  "password": "short"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.username").value("Incorrect name"))
                .andExpect(jsonPath("$.email").value("Incorrect email"))
                .andExpect(jsonPath("$.password").value(
                        "Password must contain at least 8 characters"));
    }

    @Test
    void shouldReturnPathVariableValidationErrors() throws Exception {
        mockMvc.perform(get("/users/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("getUser.id: must be greater than 0"));
    }

    private void initBooks() {
        when(bookClient.findById(1L)).thenReturn(Optional.of(new BookResponse(
                1L,
                "The Great Gatsby",
                "F. Scott Fitzgerald",
                "Classic",
                "A Jazz Age story.",
                12.99
        )));
        when(bookClient.findById(2L)).thenReturn(Optional.of(new BookResponse(
                2L,
                "Clean Code",
                "Robert C. Martin",
                "Programming",
                "Practical programming advice.",
                29.99
        )));
    }

    private JwtAuthenticationToken jwtUser(Long userId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "HS256")
                .subject("user-" + userId)
                .claim("user_id", userId)
                .build();
        return new JwtAuthenticationToken(jwt);
    }
}
