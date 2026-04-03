package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.LinkedHashSet;

@Component
@Qualifier("filmDbStorage")
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final LikeDbStorage likeDbStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, LikeDbStorage likeDbStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.likeDbStorage = likeDbStorage;
    }

    private final RowMapper<Film> mapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("mpa_name"));
        film.setMpa(mpa);

        film.setGenres(new LinkedHashSet<>());
        film.setLikes(new LinkedHashSet<>());
        return film;
    };

    @Override
    public Collection<Film> findAll() {
        List<Film> films = jdbcTemplate.query(
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                        "m.mpa_id, m.name AS mpa_name " +
                        "FROM films f " +
                        "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                        "ORDER BY f.film_id",
                mapper
        );
        films.forEach(this::loadDetails);
        return films;
    }

    @Override
    public Film create(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        int id = keyHolder.getKey().intValue();
        film.setId(id);

        updateFilmGenres(id, film.getGenres());
        return findById(id);
    }

    @Override
    public Film update(Film film) {
        int rows = jdbcTemplate.update(
                "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?",
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        if (rows == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        updateFilmGenres(film.getId(), film.getGenres());
        return findById(film.getId());
    }

    @Override
    public Film findById(int id) {
        List<Film> films = jdbcTemplate.query(
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                        "m.mpa_id, m.name AS mpa_name " +
                        "FROM films f " +
                        "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                        "WHERE f.film_id = ?",
                mapper,
                id
        );
        if (films.isEmpty()) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        Film film = films.get(0);
        loadDetails(film);
        return film;
    }

    @Override
    public void delete(int id) {
        int rows = jdbcTemplate.update("DELETE FROM films WHERE film_id = ?", id);
        if (rows == 0) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
    }

    private void loadDetails(Film film) {
        loadGenres(film);
        loadLikes(film);
    }

    private void loadGenres(Film film) {
        List<Genre> genres = jdbcTemplate.query(
                "SELECT g.genre_id, g.name " +
                        "FROM film_genres fg " +
                        "JOIN genres g ON fg.genre_id = g.genre_id " +
                        "WHERE fg.film_id = ? " +
                        "ORDER BY g.genre_id",
                (rs, rowNum) -> {
                    Genre genre = new Genre();
                    genre.setId(rs.getInt("genre_id"));
                    genre.setName(rs.getString("name"));
                    return genre;
                },
                film.getId()
        );
        film.setGenres(new LinkedHashSet<>(genres));
    }

    private void loadLikes(Film film) {
        List<Integer> likes = likeDbStorage.findLikes(film.getId());
        film.setLikes(new LinkedHashSet<>(likes));
    }

    private void updateFilmGenres(int filmId, Set<Genre> genres) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
        if (genres == null || genres.isEmpty()) {
            return;
        }
        for (Genre genre : genres) {
            jdbcTemplate.update(
                    "MERGE INTO film_genres (film_id, genre_id) KEY (film_id, genre_id) VALUES (?, ?)",
                    filmId,
                    genre.getId()
            );
        }
    }
}