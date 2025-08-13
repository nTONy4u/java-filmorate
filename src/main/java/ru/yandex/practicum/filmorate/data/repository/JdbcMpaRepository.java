package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository("MpaDao")
public class JdbcMpaRepository implements MpaDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcMpaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MpaRating> getAllMpaRatings() {
        String sql = "SELECT * FROM mpa_ratings ORDER BY mpa_id";
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    @Override
    public MpaRating getMpaById(int id) {
        try {
            String sql = "SELECT * FROM mpa_ratings WHERE mpa_id = ?";
            return jdbcTemplate.queryForObject(sql, this::mapRowToMpa, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Рейтинг MPA с id=" + id + " не найден");
        }
    }

    private MpaRating mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return new MpaRating(
                rs.getInt("mpa_id"),
                rs.getString("name"),
                rs.getString("description")
        );
    }
}