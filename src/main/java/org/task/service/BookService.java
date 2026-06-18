package org.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.task.dao.BookDao;
import org.task.model.Book;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private final BookDao bookDao;

    public void create(Book book) {
        bookDao.create(book);
    }

    public Book findById(long id) {
        return bookDao.findById(id);
    }

    public List<Book> findAll() {
        return bookDao.findAll();
    }

    public Book findByIdWithReviews(long id) {
        return bookDao.findByIdWithReviews(id);
    }

    public void update(Book book) {
        bookDao.update(book);
    }

    public void deleteById(long id) {
        bookDao.deleteById(id);
    }
}
