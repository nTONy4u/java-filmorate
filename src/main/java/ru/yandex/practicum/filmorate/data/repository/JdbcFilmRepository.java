package ru.yandex.practicum.filmorate.data.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.data.dao.FilmDao;
import ru.yandex.practicum.filmorate.data.dao.GenreDao;
import ru.yandex.practicum.filmorate.data.dao.MpaDao;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository("FilmDao")
public class JdbcFilmRepository implements FilmDao {
    private final JdbcTemplate jdbcTemplate;
    private final GenreDao genreDao;
    private final MpaDao mpaDao;

    @Autowired
    public JdbcFilmRepository(JdbcTemplate jdbcTemplate, GenreDao genreDao, MpaDao mpaDao) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreDao = genreDao;
        this.mpaDao = mpaDao;
    }

    @Override
    public Film addFilm(Film film) {
        log.debug("Добавление фильма: name={}, releaseDate={}", film.getName(), film.getReleaseDate());
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement stmt = connection.prepareStatement(sql, new String[]{"film_id"});
                stmt.setString(1, film.getName());
                stmt.setString(2, film.getDescription());
                stmt.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
                stmt.setInt(4, film.getDuration());
                stmt.setInt(5, film.getMpa().getId());
                return stmt;
            }, keyHolder);

            film.setId(keyHolder.getKey().longValue());
            log.info("Фильм добавлен: id={}, name={}", film.getId(), film.getName());

            saveFilmGenres(film);
            return film;
        } catch (Exception e) {
            log.error("Ошибка при добавлении фильма", e);
            throw e;
        }
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? " +
                "WHERE film_id = ?";
        int updated = jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        if (updated == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveFilmGenres(film);

        return film;
    }

    @Override
    public Film getFilm(long id) {
        String sql = "SELECT * FROM films WHERE film_id = ?";
        return jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT * FROM films ORDER BY film_id ASC";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public void addLike(long filmId, long userId) {
        log.debug("Добавление лайка: filmId={}, userId={}", filmId, userId);
        if (!filmExists(filmId)) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }

        String checkUserSql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer userCount = jdbcTemplate.queryForObject(checkUserSql, Integer.class, userId);
        if (userCount == null || userCount == 0) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        log.debug("Получение {} популярных фильмов", count);
        String sql = "SELECT f.*, COUNT(l.user_id) as likes_count FROM films f " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, this::mapRowToFilm, count);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        int mpaId = rs.getInt("mpa_id");
        film.setMpa(mpaDao.getMpaById(mpaId));

        Set<Genre> genres = genreDao.getGenresByFilmId(film.getId());
        film.setGenres(genres);

        String likesSql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.queryForList(likesSql, Long.class, film.getId());
        film.getLikes().addAll(likes);

        return film;
    }

    private void saveFilmGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            log.debug("Сохранение {} жанров для фильма {}", film.getGenres().size(), film.getId());
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

            jdbcTemplate.batchUpdate(sql, film.getGenres().stream()
                    .map(genre -> {
                        log.trace("Добавление жанра {} для фильма {}", genre.getId(), film.getId());
                        return new Object[]{film.getId(), genre.getId()};
                    })
                    .collect(Collectors.toList()));
        }
    }

    @Override
    public boolean filmExists(long filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        return count != null && count > 0;
    }
}