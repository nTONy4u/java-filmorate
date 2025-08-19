package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Genre {
    private long id;
    private String name;

    public Genre() {
    }

    public Genre(long id, String name) {
        this.id = id;
        this.name = name;
    }
}