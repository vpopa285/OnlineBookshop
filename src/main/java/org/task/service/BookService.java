package org.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.task.SearchType;
import org.task.model.Book;
import org.task.repositories.BookRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookRepository bookRepository;

    public void create(Book book) {
        bookRepository.save(book);
    }

    public Optional<Book> findById(long id) {
        return bookRepository.findById(id);
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

    public void deleteById(long id) {
        bookRepository.deleteById(id);
    }
}
