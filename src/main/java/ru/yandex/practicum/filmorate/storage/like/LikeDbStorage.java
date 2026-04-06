package ru.yandex.practicum.filmorate.storage.like;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LikeDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public LikeDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addLike(int filmId, int userId) {
        jdbcTemplate.update(
                "MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)",
                filmId,
                userId
        );
    }

    public void removeLike(int filmId, int userId) {
        jdbcTemplate.update(
                "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?",
                filmId,
                userId
        );
    }

    public List<Integer> findLikes(int filmId) {
        return jdbcTemplate.query(
                "SELECT user_id FROM film_likes WHERE film_id = ? ORDER BY user_id",
                (rs, rowNum) -> rs.getInt("user_id"),
                filmId
        );
    }
}