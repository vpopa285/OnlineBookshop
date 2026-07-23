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
import org.task.service.UserService;

import static org.assertj.core.api.BDDAssumptions.given;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BookshopApplication.class)
@ActiveProfiles("test")
@Sql(scripts = "/db/init.sql")
class BookControllerTest {
    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;
    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void shouldRunBookCrudHappyPaths() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("The Great Gatsby"));

        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Domain-Driven Design",
                                  "author": "Eric Evans",
                                  "genre": "Programming",
                                  "content": "Tackling complexity.",
                                  "price": 42.50
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(4))
                .andExpect(jsonPath("$.title").value("Domain-Driven Design"));

        mockMvc.perform(put("/books/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Refactoring",
                                  "author": "Martin Fowler",
                                  "genre": "Programming",
                                  "content": "Improving existing code.",
                                  "price": 40.00
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Refactoring"));

        mockMvc.perform(patch("/books/4")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":39.99}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(39.99));

        mockMvc.perform(delete("/books/4"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRunBookReviewHappyPaths() throws Exception {
        mockMvc.perform(get("/books/1/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        mockMvc.perform(post("/books/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 3,
                                  "rate": 3,
                                  "comment": "Readable."
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.userId").value(3));
    }

    @Test
    void shouldKeepApiBookPrefixWorking() throws Exception {
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("The Great Gatsby"));
    }

    @Test
    void shouldReturnNotFoundWhenBookDoesNotExist() throws Exception {
        mockMvc.perform(get("/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Book with id 999 does not exist"));
    }

    @Test
    void shouldReturnBookValidationErrors() throws Exception {
        mockMvc.perform(post("/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "author": "",
                                  "genre": "",
                                  "content": "",
                                  "price": -1
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title[0]").value("Title is required"))
                .andExpect(jsonPath("$.author[0]").value("Author is required"))
                .andExpect(jsonPath("$.genre[0]").value("Genre is required"))
                .andExpect(jsonPath("$.content[0]").value("Content is required"))
                .andExpect(jsonPath("$.price[0]").value("Price must be positive"));
    }

    @Test
    void shouldReturnBookReviewValidationErrors() throws Exception {
        mockMvc.perform(post("/books/1/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": -1,
                                  "rate": 6,
                                  "comment": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.userId[0]").value("User id must be positive"))
                .andExpect(jsonPath("$.rate[0]").value("Rate must be at most 5"))
                .andExpect(jsonPath("$.comment[0]").value("Comment is required"));
    }
}
