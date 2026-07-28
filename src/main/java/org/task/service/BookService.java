package org.task.service;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.task.SearchType;
import org.task.dto.PageResponse;
import org.task.dto.filter.BookFilter;
import org.task.dto.request.BookRequest;
import org.task.dto.request.PriceUpdateRequest;
import org.task.dto.response.BookResponse;
import org.task.dto.specification.BookSpecification;
import org.task.exceptions.BookNotFoundException;
import org.task.model.Book;
import org.task.repositories.BookRepository;

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

    public BookResponse findResponseById(Long id) {
        return bookToResponse(getBookOrThrow(id));
    }

    public PageResponse<BookResponse> findAllResponses(BookFilter filter, Pageable pageable) {
        Specification<Book> specification = BookSpecification.withFilter(filter);

        Page<BookResponse> page = bookRepository.findAll(specification, pageable)
                .map(BookService::bookToResponse);

        return PageResponse.from(page);
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
        Book existingBook = getBookOrThrow(id);
        Book book = new Book(
                existingBook.getId(),
                bookRequest.title(),
                bookRequest.author(),
                bookRequest.genre(),
                bookRequest.content(),
                bookRequest.price(),
                existingBook.getReviews()
        );

        return bookToResponse(bookRepository.save(book));
    }

    public BookResponse update(Long id, PriceUpdateRequest request) {
        Book book = getBookOrThrow(id);
        book.setPrice(request.price());
        return bookToResponse(bookRepository.save(book));
    }

    public void deleteById(long id) {
        bookRepository.delete(getBookOrThrow(id));
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

    public static BookResponse bookToResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getContent(),
                book.getPrice()
        );
    }

    private Book getBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }
}
