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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = BookshopApplication.class)
@ActiveProfiles("test")
@Sql(scripts = "/db/init.sql")
class ReviewControllerTest {
    @Autowired
    WebApplicationContext context;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void shouldRunReviewCrudHappyPaths() throws Exception {
        mockMvc.perform(get("/reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)));

        mockMvc.perform(get("/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("An absolute masterpiece."));

        mockMvc.perform(patch("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rate": 4,
                                  "comment": "Still excellent."
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rate").value(4))
                .andExpect(jsonPath("$.comment").value("Still excellent."));

        mockMvc.perform(delete("/reviews/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldKeepApiReviewPrefixWorking() throws Exception {
        mockMvc.perform(get("/api/reviews/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(1));
    }

    @Test
    void shouldFilterReviewsByRateParameters() throws Exception {
        mockMvc.perform(get("/reviews").param("exactRate", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].rate").value(5));

        mockMvc.perform(get("/reviews")
                        .param("minRate", "4")
                        .param("maxRate", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].rate").value(4));
    }

    @Test
    void shouldSortReviewsByRate() throws Exception {
        mockMvc.perform(get("/reviews").param("sort", "rate,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].rate").value(4))
                .andExpect(jsonPath("$.sort[0]").value("rate,asc"));
    }

    @Test
    void shouldReturnNotFoundWhenReviewDoesNotExist() throws Exception {
        mockMvc.perform(get("/reviews/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").value("Review with id 999 does not exist"));
    }

    @Test
    void shouldReturnReviewValidationErrors() throws Exception {
        mockMvc.perform(patch("/reviews/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rate": 6,
                                  "comment": ""
                                }
                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.rate").value("Rate must be at most 5"))
                .andExpect(jsonPath("$.comment").value("Comment is required"));
    }

    @Test
    void shouldReturnPathVariableValidationErrors() throws Exception {
        mockMvc.perform(get("/reviews/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("getReview.reviewId: must be greater than 0"));
    }
}
