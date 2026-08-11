package org.task.service;

import org.junit.jupiter.api.Test;
import org.task.dto.request.BookRequest;
import org.task.dto.request.PriceUpdateRequest;
import org.task.model.Book;
import org.task.repositories.BookRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookServiceTest {
    private final BookRepository bookRepository = mock(BookRepository.class);
    private final BookService bookService = new BookService(bookRepository);

    @Test
    void shouldCreateBookResponse() {
        Book savedBook = Book.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert Martin")
                .genre("Programming")
                .content("content")
                .price(10)
                .build();

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        var response = bookService.createResponse(new BookRequest(
                "Clean Code",
                "Robert Martin",
                "Programming",
                "content",
                10
        ));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Clean Code");
    }

    @Test
    void shouldUpdateBookPriceWhenBookExists() {
        Book book = Book.builder()
                .id(1L)
                .title("Book")
                .author("Author")
                .genre("Genre")
                .content("content")
                .price(10)
                .build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        var response = bookService.update(1L, new PriceUpdateRequest(15));

        assertThat(response.price()).isEqualTo(15);
        verify(bookRepository).save(book);
    }
}
