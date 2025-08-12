package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long idCounter = 1;

    @Override
    public User addUser(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User getUser(long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        return users.get(id);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void addFriend(long userId, long friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    @Override
    public List<User> getFriends(long userId) {
        User user = getUser(userId);
        return user.getFriends().stream()
                .map(this::getUser)
                .toList();
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        User user = getUser(userId);
        User otherUser = getUser(otherId);

        return user.getFriends().stream()
                .filter(otherUser.getFriends()::contains)
                .map(this::getUser)
                .toList();
    }
}