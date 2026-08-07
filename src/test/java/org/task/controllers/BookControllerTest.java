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

import static org.hamcrest.Matchers.hasSize;
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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void shouldRunBookCrudHappyPaths() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));

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
    void shouldFilterBooksBySearchParameters() throws Exception {
        mockMvc.perform(get("/books")
                        .param("title", "code")
                        .param("author", "martin")
                        .param("genres", "Programming")
                        .param("priceMin", "25")
                        .param("priceMax", "35"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"));
    }

    @Test
    void shouldFilterBooksByMultipleExactGenres() throws Exception {
        mockMvc.perform(get("/books")
                        .param("genres", "Classic", "Programming")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"))
                .andExpect(jsonPath("$.content[1].title").value("The Great Gatsby"));
    }

    @Test
    void shouldSortBooksByAtLeastTwoFields() throws Exception {
        mockMvc.perform(get("/books").param("sort", "price,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"))
                .andExpect(jsonPath("$.sort[0]").value("price,desc"));

        mockMvc.perform(get("/books").param("sort", "author,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].author").value("Robert C. Martin"))
                .andExpect(jsonPath("$.sort[0]").value("author,desc"));
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
                .andExpect(jsonPath("$.title").value("Title is required"))
                .andExpect(jsonPath("$.author").value("Author is required"))
                .andExpect(jsonPath("$.genre").value("Genre is required"))
                .andExpect(jsonPath("$.content").value("Content is required"))
                .andExpect(jsonPath("$.price").value("Price must be positive"));
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
                .andExpect(jsonPath("$.userId").value("User id must be positive"))
                .andExpect(jsonPath("$.rate").value("Rate must be at most 5"))
                .andExpect(jsonPath("$.comment").value("Comment is required"));
    }
}
