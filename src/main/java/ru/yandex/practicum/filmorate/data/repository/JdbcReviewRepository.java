package ru.yandex.practicum.filmorate.data.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.dao.ReviewDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Repository("ReviewDao")
public class JdbcReviewRepository implements ReviewDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcReviewRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Review addReview(Review review) {
        log.debug("Добавление отзыва: filmId={}, userId={}", review.getFilmId(), review.getUserId());
        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"review_id"});
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setLong(3, review.getUserId());
            stmt.setLong(4, review.getFilmId());
            stmt.setInt(5, review.getUseful());
            return stmt;
        }, keyHolder);

        review.setReviewId(keyHolder.getKey().longValue());
        log.info("Отзыв добавлен: id={}", review.getReviewId());
        return review;
    }

    @Override
    public Review updateReview(Review review) {
        String sql = "UPDATE reviews SET content = ?, is_positive = ?, useful = ? WHERE review_id = ?";
        int updated = jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getUseful(),
                review.getReviewId());

        if (updated == 0) {
            throw new NotFoundException("Отзыв с id=" + review.getReviewId() + " не найден");
        }
        return review;
    }

    @Override
    public boolean deleteReview(long reviewId) {
        String sql = "DELETE FROM reviews WHERE review_id = ?";
        int deleted = jdbcTemplate.update(sql, reviewId);
        return deleted > 0;
    }

    @Override
    public Optional<Review> getReviewById(long reviewId) {
        try {
            String sql = "SELECT * FROM reviews WHERE review_id = ?";
            Review review = jdbcTemplate.queryForObject(sql, this::mapRowToReview, reviewId);
            return Optional.ofNullable(review);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> getReviewsByFilmId(Long filmId, int count) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, filmId, count);
    }

    @Override
    public List<Review> getAllReviews(int count) {
        String sql = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
        return jdbcTemplate.query(sql, this::mapRowToReview, count);
    }

    @Override
    public void addLike(long reviewId, long userId) {
        String checkSql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer exists = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);

        if (exists == null || exists == 0) {
            String sql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, true)";
            jdbcTemplate.update(sql, reviewId, userId);

            String updateUsefulSql = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
            jdbcTemplate.update(updateUsefulSql, reviewId);
        }
    }

    @Override
    public void addDislike(long reviewId, long userId) {
        String checkSql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer exists = jdbcTemplate.queryForObject(checkSql, Integer.class, reviewId, userId);

        if (exists == null || exists == 0) {
            String sql = "INSERT INTO review_likes (review_id, user_id, is_like) VALUES (?, ?, false)";
            jdbcTemplate.update(sql, reviewId, userId);

            String updateUsefulSql = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
            jdbcTemplate.update(updateUsefulSql, reviewId);
        }
    }

    @Override
    public void removeLike(long reviewId, long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = true";
        int deleted = jdbcTemplate.update(sql, reviewId, userId);

        if (deleted > 0) {
            String updateUsefulSql = "UPDATE reviews SET useful = useful - 1 WHERE review_id = ?";
            jdbcTemplate.update(updateUsefulSql, reviewId);
        }
    }

    @Override
    public void removeDislike(long reviewId, long userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND is_like = false";
        int deleted = jdbcTemplate.update(sql, reviewId, userId);

        if (deleted > 0) {
            String updateUsefulSql = "UPDATE reviews SET useful = useful + 1 WHERE review_id = ?";
            jdbcTemplate.update(updateUsefulSql, reviewId);
        }
    }

    @Override
    public boolean reviewExists(long reviewId) {
        String sql = "SELECT COUNT(*) FROM reviews WHERE review_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId);
        return count != null && count > 0;
    }

    @Override
    public boolean userAlreadyReacted(long reviewId, long userId) {
        String sql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, reviewId, userId);
        return count != null && count > 0;
    }

    private Review mapRowToReview(ResultSet rs, int rowNum) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getLong("review_id"));
        review.setContent(rs.getString("content"));
        review.setIsPositive(rs.getBoolean("is_positive"));
        review.setUserId(rs.getLong("user_id"));
        review.setFilmId(rs.getLong("film_id"));
        review.setUseful(rs.getInt("useful"));

        String likesSql = "SELECT user_id FROM review_likes WHERE review_id = ? AND is_like = true";
        List<Long> likes = jdbcTemplate.queryForList(likesSql, Long.class, review.getReviewId());
        review.getLikes().addAll(likes);

        String dislikesSql = "SELECT user_id FROM review_likes WHERE review_id = ? AND is_like = false";
        List<Long> dislikes = jdbcTemplate.queryForList(dislikesSql, Long.class, review.getReviewId());
        review.getDislikes().addAll(dislikes);

        return review;
    }
}