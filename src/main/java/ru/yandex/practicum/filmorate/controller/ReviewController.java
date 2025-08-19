package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@Slf4j
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        log.info("Добавление отзыва: {}", review);
        return reviewService.addReview(review);
    }

    @PutMapping
    public Review updateReview(@Valid @RequestBody Review review) {
        log.info("Обновление отзыва: {}", review);
        return reviewService.updateReview(review);
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable long id) {
        log.info("Удаление отзыва с id={}", id);
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable long id) {
        log.info("Получение отзыва с id={}", id);
        return reviewService.getReviewById(id);
    }

    @GetMapping
    public List<Review> getReviews(
            @RequestParam(required = false) Long filmId,
            @RequestParam(defaultValue = "10", required = false) int count) {
        log.info("Получение отзывов: filmId={}, count={}", filmId, count);
        return reviewService.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Добавление лайка отзыву {} от пользователя {}", id, userId);
        reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void addDislike(@PathVariable long id, @PathVariable long userId) {
        log.info("Добавление дизлайка отзыву {} от пользователя {}", id, userId);
        reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Удаление лайка у отзыва {} от пользователя {}", id, userId);
        reviewService.removeLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable long id, @PathVariable long userId) {
        log.info("Удаление дизлайка у отзыва {} от пользователя {}", id, userId);
        reviewService.removeDislike(id, userId);
    }
}