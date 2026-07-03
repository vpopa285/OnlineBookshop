package org.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.task.dao.BookDao;
import org.task.dao.ReviewDao;
import org.task.model.Book;
import org.task.model.Review;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewDao reviewDao;
    private final BookDao bookDao;

    public void create(long bookId, Review review) {
        Book book = bookDao.findById(bookId).orElse(null);
        if(book != null) {
            review.setBook(book);
            reviewDao.create(bookId, review);
        }
    }

    public List<Review> findByBookId(long bookId) {
        return reviewDao.findByBookId(bookId);
    }

    public void deleteByUserAndBookId(long userId, long bookId) {
        reviewDao.deleteByUserAndBookId(userId, bookId);
    }
}
