package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.data.dao.GenreDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Set;

@Service
public class GenreService {
    private final GenreDao genreDao;

    @Autowired
    public GenreService(@Qualifier("GenreDao") GenreDao genreDao) {
        this.genreDao = genreDao;
    }

    public List<Genre> getAllGenres() {
        return genreDao.getAllGenres();
    }

    public Genre getGenreById(int genreId) {
        return genreDao.getGenreById(genreId)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + genreId + " не найден"));
    }

    public Set<Genre> getGenresByFilmId(long filmId) {
        return genreDao.getGenresByFilmId(filmId);
    }

    public List<Genre> getByIds(List<Long> ids) {
        return genreDao.getByIds(ids);
    }
}