package org.task.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.task.UserServiceApplication;
import org.task.client.BookClient;
import org.task.dto.response.BookResponse;

import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = UserServiceApplication.class)
@ActiveProfiles("test")
@Sql(scripts = "/db/init.sql")
class OrderControllerTest {
    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private BookClient bookClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        initBooks();
    }

    @Test
    void shouldRunOrderCreateAndReadHappyPaths() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "bookId": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.username").value("alice_reads"))
                .andExpect(jsonPath("$.books[0].id").value(2));

        mockMvc.perform(get("/orders/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3));
    }

    @Test
    void shouldKeepApiOrderPrefixWorking() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "bookId": 2
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldFilterOrdersByUsernameAndDates() throws Exception {
        mockMvc.perform(get("/orders")
                        .param("username", "alice")
                        .param("minDate", "2024-01-01T00:00:00")
                        .param("maxDate", "2024-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].user.username").value("alice_reads"));

        mockMvc.perform(get("/orders")
                        .param("exactDate", "2024-02-10T12:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].user.username").value("bob_pages"));
    }

    @Test
    void shouldSortOrdersByCreatedAt() throws Exception {
        mockMvc.perform(get("/orders").param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].user.username").value("bob_pages"))
                .andExpect(jsonPath("$.sort[0]").value("createdAt,desc"));
    }

    @Test
    void shouldReturnNotFoundWhenOrderDoesNotExist() throws Exception {
        mockMvc.perform(get("/orders/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Order with id 999 does not exist"));
    }

    @Test
    void shouldReturnOrderValidationErrors() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": -1,
                                  "bookId": -2
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.userId").value("User id must be positive"))
                .andExpect(jsonPath("$.bookId").value("Book id must be positive"));
    }

    @Test
    void shouldReturnNotFoundWhenOrderUserDoesNotExist() throws Exception {
        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 999,
                                  "bookId": 1
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("User with id 999 does not exist"));
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
}
