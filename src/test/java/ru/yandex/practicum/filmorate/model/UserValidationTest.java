package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class UserValidationTest {
    private Validator validator;
    private User validUser;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validUser = new User();
        validUser.setEmail("valid@email.com");
        validUser.setLogin("validLogin");
        validUser.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void whenAllFieldsCorrect() {
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertEquals(0, violations.size());
    }

    @Test
    void whenEmailIsBlank() {
        validUser.setEmail("");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertEquals(1, violations.size());
        assertEquals("Электронная почта не может быть пустой", violations.iterator().next().getMessage());
    }

    @Test
    void whenEmailInvalidFormat() {
        validUser.setEmail("invalid-email");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertEquals(1, violations.size());
        assertEquals("Электронная почта должна быть корректной", violations.iterator().next().getMessage());
    }

    @Test
    void whenLoginIsBlank() {
        validUser.setLogin("");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);

        assertEquals(2, violations.size()); // Проверяем что есть 2 нарушения (NotBlank и Pattern)

        Set<String> messages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());

        assertTrue(messages.contains("Логин не может быть пустым"));
        assertTrue(messages.contains("Логин не может содержать пробелы"));
    }

    @Test
    void whenLoginContainsSpaces() {
        validUser.setLogin("login with spaces");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertEquals(1, violations.size());
        assertEquals("Логин не может содержать пробелы", violations.iterator().next().getMessage());
    }

    @Test
    void whenBirthdayInFuture() {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertEquals(1, violations.size());
        assertEquals("Дата рождения не может быть в будущем", violations.iterator().next().getMessage());
    }

    @Test
    void whenBirthdayIsToday() {
        validUser.setBirthday(LocalDate.now());
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertEquals(0, violations.size());
    }

    @Test
    void whenNameIsNull() {
        validUser.setName(null);
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertEquals(0, violations.size());
    }

    @Test
    void whenNameIsEmpty() {
        validUser.setName("");
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertEquals(0, violations.size());
    }
}