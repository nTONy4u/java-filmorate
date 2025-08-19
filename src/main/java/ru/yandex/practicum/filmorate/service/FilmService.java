package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.data.dao.FilmDao;
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
    private final GenreService genreService;
    private final MpaService mpaService;

    @Autowired
    public FilmService(
            @Qualifier("FilmDao") FilmDao filmDao,
            UserService userService,
            GenreService genreService,
            MpaService mpaService) {
        this.filmDao = filmDao;
        this.userService = userService;
        this.genreService = genreService;
        this.mpaService = mpaService;
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        try {
            mpaService.getMpaById(film.getMpa().getId());
        } catch (NotFoundException e) {
            throw new NotFoundException("MPA рейтинг с id=" + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                try {
                    genreService.getGenreById((int) genre.getId());
                } catch (NotFoundException e) {
                    throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
                }
            }
        }

        return filmDao.addFilm(film);
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        return filmDao.updateFilm(film);
    }

    public Film getFilm(long id) {
        Film film = filmDao.getFilm(id);
        Set<Genre> genres = genreService.getGenresByFilmId(id);
        film.setGenres(genres);
        return film;
    }

    public List<Film> getAllFilms() {
        List<Film> films = filmDao.getAllFilms();
        films.forEach(film -> {
            Set<Genre> genres = genreService.getGenresByFilmId(film.getId());
            film.setGenres(genres);
        });
        return films;
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
        return genreService.getGenreById(genreId);
    }

    public List<Genre> getAllGenres() {
        return genreService.getAllGenres();
    }

    private void validateFilm(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == 0) {
            throw new NotFoundException("MPA рейтинг не указан");
        }
        try {
            mpaService.getMpaById(film.getMpa().getId());
        } catch (NotFoundException e) {
            throw new NotFoundException("MPA рейтинг с id=" + film.getMpa().getId() + " не найден");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Long> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());

            List<Genre> existingGenres = genreService.getByIds(new ArrayList<>(genreIds));
            if (existingGenres.size() != genreIds.size()) {
                Set<Long> existingIds = existingGenres.stream()
                        .map(Genre::getId)
                        .collect(Collectors.toSet());

                genreIds.removeAll(existingIds);
                throw new NotFoundException("Жанры с id=" + genreIds + " не найдены");
            }
        }
    }
    public List<Film> searchFilms(String query, List<String> searchBy) {
        if (query == null || query.isBlank()) {
            return new ArrayList<>();
        }

        List<Film> films = filmDao.searchFilms(query, searchBy);

        films.forEach(film -> {
            Set<Genre> genres = genreService.getGenresByFilmId(film.getId());
            film.setGenres(genres);
        });

        return films;
    }
}