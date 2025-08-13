package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.data.dao.UserDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Service
public class UserService {
    private final UserDao userDao;

    public UserService(@Qualifier("UserDao") UserDao userDao) {
        this.userDao = userDao;
    }

    public User addUser(User user) {
        validateUser(user);
        return userDao.addUser(user);
    }

    public User updateUser(User user) {
        validateUser(user);
        return userDao.updateUser(user);
    }

    public User getUser(long id) {
        return userDao.getUser(id);
    }

    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    public void addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Пользователь не может добавить себя в друзья");
        }
        userDao.addFriend(userId, friendId);
    }

    public void confirmFriend(long userId, long friendId) {
        userDao.confirmFriend(userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        getUser(userId);
        getUser(friendId);
        if (!userDao.userExists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!userDao.userExists(friendId)) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }
        userDao.removeFriend(userId, friendId);
    }

    public List<User> getFriends(long userId) {
        if (!userDao.userExists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        return userDao.getFriends(userId);
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        return userDao.getCommonFriends(userId, otherId);
    }

    private void validateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }
}