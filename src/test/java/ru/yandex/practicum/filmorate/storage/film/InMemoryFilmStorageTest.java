package ru.yandex.practicum.filmorate.storage.film;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryFilmStorageTest {
    private InMemoryFilmStorage storage;
    private Film film1;
    private Film film2;

    @BeforeEach
    void setUp() {
        storage = new InMemoryFilmStorage();

        film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Description 1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(120);

        film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2005, 5, 5));
        film2.setDuration(90);
    }

    @Test
    void addLike() {
        Film addedFilm = storage.addFilm(film1);
        storage.addLike(addedFilm.getId(), 1);

        List<Film> popularFilms = storage.getPopularFilms(1);
        assertEquals(1, popularFilms.size());
        assertEquals(addedFilm.getId(), popularFilms.get(0).getId());
    }

    @Test
    void removeLike() {
        Film addedFilm = storage.addFilm(film1);
        storage.addLike(addedFilm.getId(), 1);
        storage.removeLike(addedFilm.getId(), 1);

        List<Film> popularFilms = storage.getPopularFilms(1);
        assertEquals(1, popularFilms.size()); // Фильм остается, но без лайков
    }

    @Test
    void getPopularFilms() {
        Film film1 = storage.addFilm(this.film1);
        Film film2 = storage.addFilm(this.film2);

        storage.addLike(film1.getId(), 1);
        storage.addLike(film1.getId(), 2);
        storage.addLike(film2.getId(), 1);

        List<Film> popularFilms = storage.getPopularFilms(2);
        assertEquals(film1.getId(), popularFilms.get(0).getId());
        assertEquals(film2.getId(), popularFilms.get(1).getId());
    }

    @Test
    void getPopularFilmsWithEqualLikes() {
        Film film1 = storage.addFilm(this.film1);
        Film film2 = storage.addFilm(this.film2);

        storage.addLike(film1.getId(), 1);
        storage.addLike(film2.getId(), 2);

        List<Film> popularFilms = storage.getPopularFilms(2);
        assertEquals(2, popularFilms.size());
    }
}