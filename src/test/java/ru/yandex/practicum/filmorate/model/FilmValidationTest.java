package ru.yandex.practicum.filmorate.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FilmValidationTest {
    private Validator validator;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Valid description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
    }

    @Test
    void whenAllFieldsCorrect() {
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertEquals(0, violations.size());
    }

    @Test
    void whenNameIsBlank() {
        validFilm.setName("");
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertEquals(1, violations.size());
        assertEquals("Название не может быть пустым", violations.iterator().next().getMessage());
    }

    @Test
    void whenDescriptionTooLong() {
        validFilm.setDescription("a".repeat(201));
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertEquals(1, violations.size());
        assertEquals("Описание не может превышать 200 символов", violations.iterator().next().getMessage());
    }

    @Test
    void whenReleaseDateBeforeMinDate() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertEquals(1, violations.size());
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", violations.iterator()
                .next().getMessage());
    }

    @Test
    void whenReleaseDateIsMinDate_thenNoViolations() {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 28));
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertEquals(0, violations.size());
    }

    @Test
    void whenDurationIsNegative_thenViolationOccurs() {
        validFilm.setDuration(-1);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertEquals(1, violations.size());
        assertEquals("Продолжительность должна быть положительной", violations.iterator()
                .next().getMessage());
    }

    @Test
    void whenDurationIsZero_thenViolationOccurs() {
        validFilm.setDuration(0);
        Set<ConstraintViolation<Film>> violations = validator.validate(validFilm);
        assertEquals(1, violations.size());
        assertEquals("Продолжительность должна быть положительной", violations.iterator()
                .next().getMessage());
    }
}