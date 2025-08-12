package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.data.dao.MpaDao;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaDao mpaDao;

    @GetMapping
    public List<MpaRating> getAllMpaRatings() {
        return mpaDao.getAllMpaRatings();
    }

    @GetMapping("/{id}")
    public MpaRating getMpaById(@PathVariable int id) {
        return mpaDao.getMpaById(id);
    }
}