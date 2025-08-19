package ru.yandex.practicum.filmorate.data.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(JdbcMpaRepository.class)
class JdbcMpaRepositoryTest {

    private final JdbcMpaRepository mpaRepository;

    @Test
    void shouldGetAllMpaRatings() {
        List<MpaRating> ratings = mpaRepository.getAllMpaRatings();

        assertThat(ratings)
                .hasSize(5)
                .extracting(MpaRating::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void shouldGetMpaById() {
        MpaRating rating = mpaRepository.getMpaById(1);

        assertThat(rating)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "G")
                .hasFieldOrPropertyWithValue("description", "Нет возрастных ограничений");
    }

    @Test
    void shouldThrowWhenMpaNotFound() {
        assertThrows(NotFoundException.class, () -> mpaRepository.getMpaById(999));
    }

    @Test
    void shouldReturnCorrectMpaDescriptions() {
        List<MpaRating> ratings = mpaRepository.getAllMpaRatings();

        assertThat(ratings)
                .filteredOn(r -> r.getId() == 1)
                .first()
                .hasFieldOrPropertyWithValue("description", "Нет возрастных ограничений");

        assertThat(ratings)
                .filteredOn(r -> r.getId() == 2)
                .first()
                .hasFieldOrPropertyWithValue("description", "Рекомендуется присутствие родителей");

        assertThat(ratings)
                .filteredOn(r -> r.getId() == 5)
                .first()
                .hasFieldOrPropertyWithValue("description", "Лицам до 18 лет просмотр запрещён");
    }
}