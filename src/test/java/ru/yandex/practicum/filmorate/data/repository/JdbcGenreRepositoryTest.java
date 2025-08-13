package ru.yandex.practicum.filmorate.data.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Genre;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(JdbcGenreRepository.class)
class JdbcGenreRepositoryTest {

    private final JdbcGenreRepository genreRepository;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM genres");

        jdbcTemplate.update("MERGE INTO genres (genre_id, name) VALUES (1, 'Комедия')");
        jdbcTemplate.update("MERGE INTO genres (genre_id, name) VALUES (2, 'Драма')");
        jdbcTemplate.update("MERGE INTO genres (genre_id, name) VALUES (3, 'Мультфильм')");
        jdbcTemplate.update("MERGE INTO genres (genre_id, name) VALUES (4, 'Триллер')");
        jdbcTemplate.update("MERGE INTO genres (genre_id, name) VALUES (5, 'Документальный')");
        jdbcTemplate.update("MERGE INTO genres (genre_id, name) VALUES (6, 'Боевик')");
    }

    @Test
    void shouldGetAllGenres() {
        List<Genre> genres = genreRepository.getAllGenres();

        assertThat(genres)
                .isNotEmpty()
                .hasSize(6)
                .extracting(Genre::getId, Genre::getName)
                .containsExactly(
                        tuple(1L, "Комедия"),
                        tuple(2L, "Драма"),
                        tuple(3L, "Мультфильм"),
                        tuple(4L, "Триллер"),
                        tuple(5L, "Документальный"),
                        tuple(6L, "Боевик")
                );
    }

    @Test
    void shouldGetGenreById() {
        Optional<Genre> genreOptional = genreRepository.getGenreById(1);

        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> {
                    assertThat(genre.getId()).isEqualTo(1L);
                    assertThat(genre.getName()).isEqualTo("Комедия");
                });
    }

    @Test
    void shouldReturnEmptyWhenGenreNotFound() {
        Optional<Genre> genreOptional = genreRepository.getGenreById(999);

        assertThat(genreOptional).isEmpty();
    }

    @Test
    void shouldGetGenresByFilmId() {
        jdbcTemplate.update(
                "INSERT INTO films (film_id, name, description, release_date, duration) " +
                        "VALUES (1, 'Test Film', 'Test Description', ?, 120)",
                LocalDate.of(2020, 1, 1)
        );
        jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (1, 1), (1, 2)");

        Set<Genre> genres = genreRepository.getGenresByFilmId(1L);

        assertThat(genres)
                .hasSize(2)
                .extracting(Genre::getId)
                .containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void shouldGetGenresByIds() {
        List<Genre> genres = genreRepository.getByIds(List.of(1L, 3L, 5L));

        assertThat(genres)
                .hasSize(3)
                .extracting(Genre::getId, Genre::getName)
                .containsExactly(
                        tuple(1L, "Комедия"),
                        tuple(3L, "Мультфильм"),
                        tuple(5L, "Документальный")
                );
    }

    @Test
    void shouldReturnEmptyListWhenNoIdsProvided() {
        List<Genre> genres = genreRepository.getByIds(List.of());

        assertThat(genres).isEmpty();
    }
}