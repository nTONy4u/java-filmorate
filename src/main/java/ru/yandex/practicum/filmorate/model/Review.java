package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class Review {
    private long reviewId;

    @NotBlank(message = "Содержание отзыва не может быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва обязателен")
    private Boolean isPositive;

    @NotNull(message = "Пользователь обязателен")
    private Long userId;

    @NotNull(message = "Фильм обязателен")
    private Long filmId;

    private int useful = 0;
    private final Set<Long> likes = new HashSet<>();
    private final Set<Long> dislikes = new HashSet<>();
}