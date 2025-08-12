package ru.yandex.practicum.filmorate.data.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.dao.GenreDao;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository("GenreDao")
public class JdbcGenreRepository implements GenreDao {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcGenreRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres ORDER BY genre_id";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    @Override
    public Optional<Genre> getGenreById(int genreId) {
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, this::mapRowToGenre, genreId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Set<Genre> getGenresByFilmId(long filmId) {
        String sql = "SELECT g.* FROM genres g JOIN film_genres fg ON g.genre_id = fg.genre_id WHERE fg.film_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, this::mapRowToGenre, filmId));
    }

    @Override
    public List<Genre> getByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        String sql = "SELECT * FROM genres WHERE genre_id IN (%s) ORDER BY genre_id"
                .formatted(ids.stream().map(i -> "?").collect(Collectors.joining(",")));

        return jdbcTemplate.query(
                sql,
                ps -> {
                    for (int i = 0; i < ids.size(); i++) {
                        ps.setLong(i + 1, ids.get(i));
                    }
                },
                this::mapRowToGenre
        );
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {
        return new Genre(rs.getLong("genre_id"), rs.getString("name"));
    }
}