package ru.yandex.practicum.filmorate.data.dao;


import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GenreDao {

    List<Genre> getAllGenres();

    Set<Genre> getGenresByFilmId(long filmId);

    List<Genre> getByIds(List<Long> ids);

    Optional<Genre> getGenreById(int genreId);
}