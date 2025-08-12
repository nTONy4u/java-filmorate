package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.data.dao.FilmDao;
import ru.yandex.practicum.filmorate.data.dao.GenreDao;
import ru.yandex.practicum.filmorate.data.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmDao filmDao;
    private final UserService userService;
    private final GenreDao genreDao;
    private final MpaDao mpaDao;

    @Autowired
    public FilmService(
            @Qualifier("FilmDao") FilmDao filmDao,
            UserService userService,
            @Qualifier("GenreDao") GenreDao genreDao,
            @Qualifier("MpaDao") MpaDao mpaDao) {
        this.filmDao = filmDao;
        this.userService = userService;
        this.genreDao = genreDao;
        this.mpaDao = mpaDao;
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        try {
            mpaDao.getMpaById(film.getMpa().getId());
        } catch (NotFoundException e) {
            throw new NotFoundException("MPA рейтинг с id=" + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreDao.getGenreById((int) genre.getId())
                        .orElseThrow(() -> new NotFoundException("Жанр с id=" + genre.getId() + " не найден"));
            }
        }

        return filmDao.addFilm(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        return filmDao.updateFilm(film);
    }

    public Film getFilm(long id) {
        return filmDao.getFilm(id);
    }

    public List<Film> getAllFilms() {
        return filmDao.getAllFilms();
    }

    public void addLike(long filmId, long userId) {
        userService.getUser(userId);
        filmDao.addLike(filmId, userId);
    }

    public void removeLike(long filmId, long userId) {
        userService.getUser(userId);
        filmDao.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmDao.getPopularFilms(count);
    }

    public Genre getGenreById(int genreId) {
        return genreDao.getGenreById(genreId)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + genreId + " не найден"));
    }

    public List<Genre> getAllGenres() {
        return genreDao.getAllGenres();
    }

    private void validateFilm(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == 0) {
            throw new NotFoundException("MPA рейтинг не указан");
        }
        try {
            mpaDao.getMpaById(film.getMpa().getId());
        } catch (NotFoundException e) {
            throw new NotFoundException("MPA рейтинг с id=" + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            List<Genre> existingGenres = genreDao.getByIds(new ArrayList<>(genreIds));
            if (existingGenres.size() != genreIds.size()) {
                Set<Long> existingIds = existingGenres.stream()
                        .map(Genre::getId)
                        .collect(Collectors.toSet());

                genreIds.removeAll(existingIds);
                throw new NotFoundException("Жанры с id=" + genreIds + " не найдены");
            }
        }
    }
}