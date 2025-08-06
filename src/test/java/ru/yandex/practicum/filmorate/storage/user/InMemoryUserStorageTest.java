package ru.yandex.practicum.filmorate.storage.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryUserStorageTest {
    private InMemoryUserStorage storage;
    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        storage = new InMemoryUserStorage();

        user1 = new User();
        user1.setEmail("user1@mail.com");
        user1.setLogin("user1");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        user2 = new User();
        user2.setEmail("user2@mail.com");
        user2.setLogin("user2");
        user2.setBirthday(LocalDate.of(1995, 5, 5));
    }

    @Test
    void addUser() {
        User addedUser = storage.addUser(user1);

        assertNotNull(addedUser.getId());
        assertEquals(1, addedUser.getId());
        assertEquals(1, storage.getAllUsers().size());
    }

    @Test
    void addMultipleUsers() {
        User addedUser1 = storage.addUser(user1);
        User addedUser2 = storage.addUser(user2);

        assertEquals(1, addedUser1.getId());
        assertEquals(2, addedUser2.getId());
    }

    @Test
    void updateUser() {
        User addedUser = storage.addUser(user1);
        addedUser.setName("Updated Name");

        User updatedUser = storage.updateUser(addedUser);

        assertEquals("Updated Name", updatedUser.getName());
        assertEquals(1, storage.getAllUsers().size());
    }

    @Test
    void updateWhenUserNotFound() {
        user1.setId(999);

        assertThrows(NotFoundException.class, () -> storage.updateUser(user1));
    }

    @Test
    void getWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> storage.getUser(999));
    }

    @Test
    void getAllUsers() {
        assertTrue(storage.getAllUsers().isEmpty());
    }

    @Test
    void addFriend() {
        User addedUser1 = storage.addUser(user1);
        User addedUser2 = storage.addUser(user2);

        storage.addFriend(addedUser1.getId(), addedUser2.getId());

        List<User> friends = storage.getFriends(addedUser1.getId());
        assertEquals(1, friends.size());
        assertEquals(addedUser2.getId(), friends.get(0).getId());
    }

    @Test
    void addWhenAlreadyFriends() {
        User addedUser1 = storage.addUser(user1);
        User addedUser2 = storage.addUser(user2);

        storage.addFriend(addedUser1.getId(), addedUser2.getId());
        storage.addFriend(addedUser1.getId(), addedUser2.getId()); // повторное добавление

        assertEquals(1, storage.getFriends(addedUser1.getId()).size());
    }

    @Test
    void removeFriend() {
        User addedUser1 = storage.addUser(user1);
        User addedUser2 = storage.addUser(user2);

        storage.removeFriend(addedUser1.getId(), addedUser2.getId());

        assertEquals(0, storage.getFriends(addedUser1.getId()).size());
    }

    @Test
    void getFriends() {
        User addedUser = storage.addUser(user1);

        assertTrue(storage.getFriends(addedUser.getId()).isEmpty());
    }

    @Test
    void getCommonFriends() {
        User addedUser1 = storage.addUser(user1);
        User addedUser2 = storage.addUser(user2);

        assertTrue(storage.getCommonFriends(addedUser1.getId(), addedUser2.getId()).isEmpty());
    }

    @Test
    void getSameUserCommonFriends() {
        User addedUser = storage.addUser(user1);

        assertTrue(storage.getCommonFriends(addedUser.getId(), addedUser.getId()).isEmpty());
    }
}