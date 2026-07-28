package org.task.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.task.BookshopApplication;
import org.task.jdbc.JdbcExecutor;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BookshopApplication.class)
@ActiveProfiles("test")
@Sql(scripts = "/db/init.sql")
class OrderControllerTest {
    @Autowired
    WebApplicationContext context;

    @Autowired
    JdbcExecutor jdbcExecutor;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void shouldRunOrderCreateAndReadHappyPaths() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 1,
                                  "bookId": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.user.username").value("alice_reads"))
                .andExpect(jsonPath("$.books[0].id").value(2));

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
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
        seedOrdersForSearch();

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
        seedOrdersForSearch();

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

    private void seedOrdersForSearch() {
        jdbcExecutor.execute("""
                INSERT INTO orders (id, user_id, created_at)
                VALUES (10, 1, TIMESTAMP '2024-01-15 10:00:00')
                """);
        jdbcExecutor.execute("""
                INSERT INTO orders (id, user_id, created_at)
                VALUES (11, 2, TIMESTAMP '2024-02-10 12:00:00')
                """);
        jdbcExecutor.execute("INSERT INTO order_items (order_id, book_id) VALUES (10, 1)");
        jdbcExecutor.execute("INSERT INTO order_items (order_id, book_id) VALUES (11, 2)");
    }
}
