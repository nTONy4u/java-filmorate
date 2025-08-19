package ru.yandex.practicum.filmorate.data.dao;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewDao {
    Review addReview(Review review);
    Review updateReview(Review review);
    boolean deleteReview(long reviewId);
    Optional<Review> getReviewById(long reviewId);
    List<Review> getReviewsByFilmId(Long filmId, int count);
    List<Review> getAllReviews(int count);
    void addLike(long reviewId, long userId);
    void addDislike(long reviewId, long userId);
    void removeLike(long reviewId, long userId);
    void removeDislike(long reviewId, long userId);
    boolean reviewExists(long reviewId);
    boolean userAlreadyReacted(long reviewId, long userId);
}