package ru.yandex.practicum.filmorate.data.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.dao.UserDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Repository("UserDao")
public class JdbcUserRepository implements UserDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public User addUser(User user) {
        log.debug("Добавление пользователя: email={}, login={}", user.getEmail(), user.getLogin());
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"user_id"});
                stmt.setString(1, user.getEmail());
                stmt.setString(2, user.getLogin());
                stmt.setString(3, user.getName());
                stmt.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
                return stmt;
            }, keyHolder);

            user.setId(keyHolder.getKey().longValue());
            log.info("Пользователь добавлен: id={}", user.getId());
            return user;
        } catch (Exception e) {
            log.error("Ошибка при добавлении пользователя", e);
            throw e;
        }
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int updated = jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        if (updated == 0) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        return user;
    }

    @Override
    public User getUser(long id) {
        log.debug("Получение пользователя по id={}", id);
        try {
            String sql = "SELECT * FROM users WHERE user_id = ?";
            User user = jdbcTemplate.queryForObject(sql, this::mapRowToUser, id);
            log.debug("Найден пользователь: {}", user);
            return user;
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пользователь с id={} не найден", id);
            return null;
        }
    }

    @Override
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY user_id DESC";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    @Override
    public void addFriend(long userId, long friendId) {
        log.debug("Добавление в друзья: {} -> {}", userId, friendId);
        if (!userExists(userId)) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        if (!userExists(friendId)) {
            throw new NotFoundException("Пользователь с id=" + friendId + " не найден");
        }

        String sql = "INSERT INTO friends (user_id, friend_id, confirmed) VALUES (?, ?, false)";
        try {
            int rows = jdbcTemplate.update(sql, userId, friendId);
            log.info("Дружба добавлена: {} -> {} ({} rows affected)", userId, friendId, rows);
        } catch (Exception e) {
            log.error("Ошибка при добавлении в друзья", e);
            throw e;
        }
    }

    public void confirmFriend(long userId, long friendId) {
        String sql = "UPDATE friends SET confirmed = true WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, friendId, userId); // friendId → userId, т.к. подтверждает тот, кого добавили
    }

    public boolean userExists(long userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(long userId) {
        log.debug("Получение друзей пользователя {}", userId);
        String sql = "SELECT u.* FROM users u JOIN friends f ON u.user_id = f.friend_id WHERE f.user_id = ?";
        List<User> friends = jdbcTemplate.query(sql, this::mapRowToUser, userId);
        log.debug("Найдено {} друзей для пользователя {}", friends.size(), userId);
        return friends;
    }

    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f1 ON u.user_id = f1.friend_id " +
                "JOIN friends f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherId);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());

        String friendsSql = "SELECT friend_id FROM friends WHERE user_id = ?";
        List<Long> friends = jdbcTemplate.queryForList(friendsSql, Long.class, user.getId());
        user.getFriends().addAll(friends);

        return user;
    }
}