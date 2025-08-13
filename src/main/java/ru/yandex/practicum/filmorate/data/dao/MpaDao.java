package ru.yandex.practicum.filmorate.data.dao;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

public interface MpaDao {
    List<MpaRating> getAllMpaRatings();

    MpaRating getMpaById(int id);
}