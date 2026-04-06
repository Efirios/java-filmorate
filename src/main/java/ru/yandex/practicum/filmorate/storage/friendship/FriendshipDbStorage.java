package ru.yandex.practicum.filmorate.storage.friendship;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.HashMap;
import java.util.List;

@Component
public class FriendshipDbStorage {
    private final JdbcTemplate jdbcTemplate;

    public FriendshipDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        user.setFriends(new HashMap<>());
        return user;
    };

    public List<User> findFriends(int userId) {
        return jdbcTemplate.query(
                "SELECT u.user_id, u.email, u.login, u.name, u.birthday " +
                        "FROM friendships f " +
                        "JOIN users u ON f.friend_id = u.user_id " +
                        "WHERE f.user_id = ? " +
                        "ORDER BY u.user_id",
                userMapper,
                userId
        );
    }

    public List<User> findCommonFriends(int userId, int otherId) {
        return jdbcTemplate.query(
                "SELECT u.user_id, u.email, u.login, u.name, u.birthday " +
                        "FROM friendships f1 " +
                        "JOIN friendships f2 ON f1.friend_id = f2.friend_id " +
                        "JOIN users u ON u.user_id = f1.friend_id " +
                        "WHERE f1.user_id = ? AND f2.user_id = ? " +
                        "ORDER BY u.user_id",
                userMapper,
                userId,
                otherId
        );
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