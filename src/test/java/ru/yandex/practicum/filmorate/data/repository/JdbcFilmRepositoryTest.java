package ru.yandex.practicum.filmorate.data.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JdbcFilmRepository.class, JdbcUserRepository.class, JdbcMpaRepository.class})
class JdbcFilmRepositoryTest {

    @Autowired
    private JdbcFilmRepository filmRepository;

    @Autowired
    private JdbcUserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MpaRating createMpaRating(int id, String name) {
        MpaRating mpa = new MpaRating();
        mpa.setId(id);
        mpa.setName(name);
        return mpa;
    }

    private User createTestUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM friends");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN film_id RESTART WITH 1");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN user_id RESTART WITH 1");

        jdbcTemplate.execute("MERGE INTO mpa_ratings (mpa_id, name, description) VALUES " +
                "(1, 'G', 'Нет возрастных ограничений'), " +
                "(2, 'PG', 'Рекомендуется присутствие родителей'), " +
                "(3, 'PG-13', 'Детям до 13 лет просмотр не желателен'), " +
                "(4, 'R', 'Лицам до 17 лет обязательно присутствие взрослого'), " +
                "(5, 'NC-17', 'Лицам до 18 лет просмотр запрещён')");
    }

    @Test
    void shouldAddAndRemoveLike() {
        User user = createTestUser("test@mail.com", "testlogin");
        User createdUser = userRepository.addUser(user);

        Film film = filmRepository.addFilm(createTestFilm());

        filmRepository.addLike(film.getId(), createdUser.getId());

        List<Film> popular = filmRepository.getPopularFilms(1);
        assertThat(popular).hasSize(1);
        assertThat(popular.get(0).getLikes()).contains(createdUser.getId());

        filmRepository.removeLike(film.getId(), createdUser.getId());
        popular = filmRepository.getPopularFilms(1);
        assertThat(popular.get(0).getLikes()).doesNotContain(createdUser.getId());
    }

    @Test
    void shouldAddAndGetFilmById() {
        Film newFilm = new Film();
        newFilm.setName("Test Film");
        newFilm.setDescription("Test Description");
        newFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        newFilm.setDuration(120);
        newFilm.setMpa(createMpaRating(1, "G"));

        Film addedFilm = filmRepository.addFilm(newFilm);
        assertThat(addedFilm.getId()).isEqualTo(1L);

        Film foundFilm = filmRepository.getFilm(addedFilm.getId());

        assertThat(foundFilm)
                .isNotNull()
                .satisfies(film -> {
                    assertThat(film.getName()).isEqualTo("Test Film");
                    assertThat(film.getDuration()).isEqualTo(120);
                });
    }

    @Test
    void shouldUpdateFilm() {
        Film film = new Film();
        film.setName("Original");
        film.setDescription("Original Desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        film.setMpa(createMpaRating(1, "G"));

        Film addedFilm = filmRepository.addFilm(film);
        addedFilm.setName("Updated");

        Film updatedFilm = filmRepository.updateFilm(addedFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated");
    }

    @Test
    void shouldThrowWhenUpdateNonExistentFilm() {
        Film film = new Film();
        film.setId(999L);
        film.setName("Non-existent");
        film.setDescription("Should throw");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);
        film.setMpa(createMpaRating(1, "G"));

        assertThrows(NotFoundException.class, () -> filmRepository.updateFilm(film));
    }

    @Test
    void shouldGetPopularFilms() {
        User user1 = createTestUser("user1@mail.com", "user1");
        User createdUser1 = userRepository.addUser(user1);

        User user2 = createTestUser("user2@mail.com", "user2");
        User createdUser2 = userRepository.addUser(user2);

        Film film1 = createTestFilm();
        film1.setName("Film 1");
        film1 = filmRepository.addFilm(film1);

        Film film2 = createTestFilm();
        film2.setName("Film 2");
        film2 = filmRepository.addFilm(film2);

        filmRepository.addLike(film1.getId(), createdUser1.getId());
        filmRepository.addLike(film1.getId(), createdUser2.getId());
        filmRepository.addLike(film2.getId(), createdUser1.getId());

        List<Film> popular = filmRepository.getPopularFilms(2);
        assertThat(popular)
                .extracting(Film::getId)
                .containsExactly(film1.getId(), film2.getId());
    }

    @Test
    void shouldGetAllFilms() {
        Film film1 = createTestFilm();
        film1.setName("Film 1");
        film1 = filmRepository.addFilm(film1);

        Film film2 = createTestFilm();
        film2.setName("Film 2");
        film2 = filmRepository.addFilm(film2);

        List<Film> allFilms = filmRepository.getAllFilms();

        assertThat(allFilms)
                .extracting(Film::getId)
                .contains(film1.getId(), film2.getId());
    }

    private Film createTestFilm() {
        Film film = new Film();
        film.setName("Test");
        film.setDescription("Test");
        film.setReleaseDate(LocalDate.now());
        film.setDuration(100);
        film.setMpa(createMpaRating(1, "G"));
        return film;
    }
}