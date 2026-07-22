package org.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.task.SearchType;
import org.task.dto.BookRequest;
import org.task.dto.BookResponse;
import org.task.dto.PriceUpdateRequest;
import org.task.model.Book;
import org.task.repositories.BookRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    public BookResponse createResponse(BookRequest bookRequest) {
        return bookToResponse(bookRepository.save(requestToBook(null, bookRequest)));
    }

    public void create(Book book) {
        bookRepository.save(book);
    }

    public Optional<Book> findById(long id) {
        return bookRepository.findById(id);
    }

    public Optional<BookResponse> findResponseById(long id) {
        return bookRepository.findById(id)
                .map(this::bookToResponse);
    }

    public List<BookResponse> findAllResponses() {
        return bookRepository.findAll()
                .stream()
                .map(this::bookToResponse)
                .toList();
    }

    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    public Optional<Book> findByIdWithReviews(long id) {
        return bookRepository.findWithReviewsById(id);
    }

    public List<Book> findAllBooksByParam(SearchType type, String param) {
        return switch (type) {
            case TITLE -> bookRepository.searchBooksByTitle(param);
            case AUTHOR -> bookRepository.searchBooksByAuthor(param);
            case GENRE -> bookRepository.searchBooksByGenre(param);
        };
    }

    public boolean existsByTitle(String title) {
        return bookRepository.existsByTitle(title);
    }

    public long count() {
        return bookRepository.count();
    }

    public void update(Book book) {
        bookRepository.save(book);
    }

    public BookResponse update(Long id, BookRequest bookRequest) {
        return bookToResponse(bookRepository.save(requestToBook(id, bookRequest)));
    }

    public Optional<BookResponse> update(Long id, PriceUpdateRequest request) {
        return bookRepository.findById(id)
                .map(book -> {
                    book.setPrice(request.price());
                    return bookToResponse(bookRepository.save(book));
                });
    }

    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }

    private Book requestToBook(Long id, BookRequest bookRequest) {
        return Book.builder()
                .id(id)
                .title(bookRequest.title())
                .author(bookRequest.author())
                .genre(bookRequest.genre())
                .content(bookRequest.content())
                .price(bookRequest.price())
                .build();
    }

    private BookResponse bookToResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getContent(),
                book.getPrice()
        );
    }
}
