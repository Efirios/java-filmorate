package ru.yandex.practicum.filmorate.storage.friendship;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FriendshipDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public FriendshipDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addFriend(int userId, int friendId) {
        boolean reverseExists = exists(friendId, userId);

        if (reverseExists) {
            jdbcTemplate.update(
                    "MERGE INTO friendships (user_id, friend_id, status_id) KEY (user_id, friend_id) VALUES (?, ?, 2)",
                    userId, friendId
            );
            jdbcTemplate.update(
                    "UPDATE friendships SET status_id = 2 WHERE user_id = ? AND friend_id = ?",
                    friendId, userId
            );
        } else {
            jdbcTemplate.update(
                    "MERGE INTO friendships (user_id, friend_id, status_id) KEY (user_id, friend_id) VALUES (?, ?, 1)",
                    userId, friendId
            );
        }
    }

    public void removeFriend(int userId, int friendId) {
        jdbcTemplate.update(
                "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?",
                userId, friendId
        );

        if (exists(friendId, userId)) {
            jdbcTemplate.update(
                    "UPDATE friendships SET status_id = 1 WHERE user_id = ? AND friend_id = ?",
                    friendId, userId
            );
        }
    }

    public List<Integer> findFriendIds(int userId) {
        return jdbcTemplate.query(
                "SELECT friend_id FROM friendships WHERE user_id = ? ORDER BY friend_id",
                (rs, rowNum) -> rs.getInt("friend_id"),
                userId
        );
    }

    public List<Integer> findCommonFriendIds(int userId, int otherId) {
        return jdbcTemplate.query(
                "SELECT f1.friend_id " +
                        "FROM friendships f1 " +
                        "JOIN friendships f2 ON f1.friend_id = f2.friend_id " +
                        "WHERE f1.user_id = ? AND f2.user_id = ? " +
                        "ORDER BY f1.friend_id",
                (rs, rowNum) -> rs.getInt("friend_id"),
                userId,
                otherId
        );
    }

    private boolean exists(int userId, int friendId) {
        List<Integer> rows = jdbcTemplate.query(
                "SELECT 1 FROM friendships WHERE user_id = ? AND friend_id = ?",
                (rs, rowNum) -> 1,
                userId,
                friendId
        );
        return !rows.isEmpty();
    }
}