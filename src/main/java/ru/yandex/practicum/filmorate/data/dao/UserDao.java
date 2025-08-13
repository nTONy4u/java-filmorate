package ru.yandex.practicum.filmorate.data.dao;


import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserDao {
    User addUser(User user);

    User updateUser(User user);

    User getUser(long id);

    List<User> getAllUsers();

    void addFriend(long userId, long friendId);

    void confirmFriend(long userId, long friendId);

    void removeFriend(long userId, long friendId);

    List<User> getFriends(long userId);

    List<User> getCommonFriends(long userId, long otherId);

    boolean userExists(long userId);
}