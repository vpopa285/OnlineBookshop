package org.task.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.task.dao.UserDao;
import org.task.model.User;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserDao userDao;

    public void create(User user) {
        userDao.create(user);
    }

    public User findById(long id) {
        return userDao.findById(id);
    }

    public List<User> findAll() {
        return userDao.findAll();
    }

    public void update(User user) {
        userDao.update(user);
    }

    public void deleteById(long id) {
        userDao.deleteById(id);
    }
}
