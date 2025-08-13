package ru.yandex.practicum.filmorate.data.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(JdbcUserRepository.class)
class JdbcUserRepositoryTest {

    @Autowired
    private JdbcUserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM friends");
        jdbcTemplate.execute("DELETE FROM users");

        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");
    }

    private User createTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void shouldAddAndGetUserById() {
        User newUser = createTestUser("test@mail.com", "testlogin");
        User addedUser = userRepository.addUser(newUser);

        assertThat(addedUser.getId()).isEqualTo(1L);

        User foundUser = userRepository.getUser(addedUser.getId());
        assertThat(foundUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("email", "test@mail.com")
                .hasFieldOrPropertyWithValue("login", "testlogin");
    }

    @Test
    void shouldReturnNullWhenUserNotFound() {
        assertThat(userRepository.getUser(999L)).isNull();
    }

    @Test
    void shouldUpdateUser() {
        User user = createTestUser("original@mail.com", "originallogin");
        User addedUser = userRepository.addUser(user);

        addedUser.setEmail("updated@mail.com");
        addedUser.setName("Updated Name");

        User updatedUser = userRepository.updateUser(addedUser);

        assertThat(updatedUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("email", "updated@mail.com")
                .hasFieldOrPropertyWithValue("name", "Updated Name");
    }

    @Test
    void shouldThrowWhenUpdateNonExistentUser() {
        User user = createTestUser("nonexistent@mail.com", "nonexistent");
        user.setId(999L);

        assertThrows(NotFoundException.class, () -> userRepository.updateUser(user));
    }

    @Test
    void shouldAddAndRemoveFriend() {
        User user1 = userRepository.addUser(createTestUser("user1@mail.com", "user1"));
        User user2 = userRepository.addUser(createTestUser("user2@mail.com", "user2"));

        userRepository.addFriend(user1.getId(), user2.getId());
        List<User> friends = userRepository.getFriends(user1.getId());
        assertThat(friends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(user2.getId());

        userRepository.removeFriend(user1.getId(), user2.getId());
        assertThat(userRepository.getFriends(user1.getId())).isEmpty();
    }

    @Test
    void shouldThrowWhenAddFriendToNonExistentUser() {
        User existingUser = userRepository.addUser(createTestUser("existing@mail.com", "existing"));

        assertThrows(NotFoundException.class,
                () -> userRepository.addFriend(existingUser.getId(), 999L));
    }

    @Test
    void shouldFindCommonFriends() {
        User user1 = userRepository.addUser(createTestUser("user1@mail.com", "user1"));
        User user2 = userRepository.addUser(createTestUser("user2@mail.com", "user2"));
        User commonFriend = userRepository.addUser(createTestUser("common@mail.com", "common"));

        userRepository.addFriend(user1.getId(), commonFriend.getId());
        userRepository.addFriend(user2.getId(), commonFriend.getId());

        List<User> commonFriends = userRepository.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(commonFriend.getId());
    }

    @Test
    void shouldReturnEmptyListWhenNoCommonFriends() {
        User user1 = userRepository.addUser(createTestUser("user1@mail.com", "user1"));
        User user2 = userRepository.addUser(createTestUser("user2@mail.com", "user2"));

        assertThat(userRepository.getCommonFriends(user1.getId(), user2.getId())).isEmpty();
    }

    @Test
    void shouldGetAllUsers() {
        User user1 = userRepository.addUser(createTestUser("user1@mail.com", "user1"));
        User user2 = userRepository.addUser(createTestUser("user2@mail.com", "user2"));

        List<User> allUsers = userRepository.getAllUsers();
        assertThat(allUsers)
                .hasSize(2)
                .extracting(User::getId)
                .containsExactlyInAnyOrder(user1.getId(), user2.getId());
    }

    @Test
    void shouldCheckUserExists() {
        User user = userRepository.addUser(createTestUser("exists@mail.com", "exists"));

        assertAll(
                () -> assertTrue(userRepository.userExists(user.getId())),
                () -> assertFalse(userRepository.userExists(999L))
        );
    }
}