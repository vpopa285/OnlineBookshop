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
                .andExpect(jsonPath("$", hasSize(3)));

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
}
