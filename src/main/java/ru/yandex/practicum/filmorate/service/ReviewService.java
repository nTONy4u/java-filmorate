package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.data.dao.ReviewDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {
    private final ReviewDao reviewDao;
    private final UserService userService;
    private final FilmService filmService;

    @Autowired
    public ReviewService(
            @Qualifier("ReviewDao") ReviewDao reviewDao,
            UserService userService,
            FilmService filmService) {
        this.reviewDao = reviewDao;
        this.userService = userService;
        this.filmService = filmService;
    }

    public Review addReview(Review review) {
        validateReview(review);
        return reviewDao.addReview(review);
    }

    public Review updateReview(Review review) {
        validateReview(review);
        if (!reviewDao.reviewExists(review.getReviewId())) {
            throw new NotFoundException("Отзыв с id=" + review.getReviewId() + " не найден");
        }
        return reviewDao.updateReview(review);
    }

    public boolean deleteReview(long reviewId) {
        if (!reviewDao.reviewExists(reviewId)) {
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
        return reviewDao.deleteReview(reviewId);
    }

    public Review getReviewById(long reviewId) {
        Optional<Review> review = reviewDao.getReviewById(reviewId);
        return review.orElseThrow(() -> new NotFoundException("Отзыв с id=" + reviewId + " не найден"));
    }

    public List<Review> getReviews(Long filmId, int count) {
        if (filmId != null) {
            filmService.getFilm(filmId);
            return reviewDao.getReviewsByFilmId(filmId, count);
        } else {
            return reviewDao.getAllReviews(count);
        }
    }

    public void addLike(long reviewId, long userId) {
        validateReviewAndUser(reviewId, userId);
        if (reviewDao.userAlreadyReacted(reviewId, userId)) {
            throw new ValidationException("Пользователь уже оценил этот отзыв");
        }
        reviewDao.addLike(reviewId, userId);
    }

    public void addDislike(long reviewId, long userId) {
        validateReviewAndUser(reviewId, userId);
        if (reviewDao.userAlreadyReacted(reviewId, userId)) {
            throw new ValidationException("Пользователь уже оценил этот отзыв");
        }
        reviewDao.addDislike(reviewId, userId);
    }

    public void removeLike(long reviewId, long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewDao.removeLike(reviewId, userId);
    }

    public void removeDislike(long reviewId, long userId) {
        validateReviewAndUser(reviewId, userId);
        reviewDao.removeDislike(reviewId, userId);
    }

    private void validateReview(Review review) {
        if (review.getUserId() == null) {
            throw new ValidationException("Пользователь не указан");
        }
        if (review.getFilmId() == null) {
            throw new ValidationException("Фильм не указан");
        }

        userService.getUser(review.getUserId());
        filmService.getFilm(review.getFilmId());
    }

    private void validateReviewAndUser(long reviewId, long userId) {
        if (!reviewDao.reviewExists(reviewId)) {
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
        userService.getUser(userId);
    }
}