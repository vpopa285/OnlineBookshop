package org.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.task.SearchType;
import org.task.dao.BookDao;
import org.task.model.Book;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookDao bookDao;

    public void create(Book book) {
        bookDao.create(book);
    }

    public Optional<Book> findById(long id) {
        return bookDao.findById(id);
    }

    public List<Book> findAll() {
        return bookDao.findAll();
    }

    public Optional<Book> findByIdWithReviews(long id) {
        return bookDao.findByIdWithReviews(id);
    }

    public List<Book> findAllBooksByParam(SearchType searchType, String searchParam) {
        return bookDao.search(searchType, searchParam);
    }

    public boolean existsByTitle(String title) {
        return bookDao.existsByTitle(title);
    }

    public void update(Book book) {
        bookDao.update(book);
    }

    public void deleteById(long id) {
        bookDao.deleteById(id);
    }

    public long count() {
        return bookDao.count();
    }
}
