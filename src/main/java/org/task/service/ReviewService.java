package org.task.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.task.dao.ReviewDao;
import org.task.model.Review;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewDao reviewDao;

    @Autowired
    public ReviewService(ReviewDao reviewDao) {
        this.reviewDao = reviewDao;
    }

    public void create(long bookId, Review review) {
        reviewDao.create(bookId, review);
    }

    public List<Review> findByBookId(long bookId) {
        return reviewDao.findByBookId(bookId);
    }

    public void deleteByUserAndBookId(long userId, long bookId) {
        reviewDao.deleteByUserAndBookId(userId, bookId);
    }
}
