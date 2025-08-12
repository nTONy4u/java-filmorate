package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.data.dao.GenreDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreDao genreDao;

    @GetMapping
    public List<Genre> getAllGenres() {
        return genreDao.getAllGenres();
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable("id") int genreId) {
        return genreDao.getGenreById(genreId)
                .orElseThrow(() -> new NotFoundException("Жанр с id=" + genreId + " не найден"));
    }
}