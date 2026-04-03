package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Qualifier("userDbStorage")
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> mapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getInt("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        user.setFriends(new HashMap<>());
        return user;
    };

    @Override
    public Collection<User> findAll() {
        List<User> users = jdbcTemplate.query(
                "SELECT user_id, email, login, name, birthday FROM users ORDER BY user_id",
                mapper
        );
        users.forEach(this::loadFriends);
        return users;
    }

    @Override
    public User create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return findById(user.getId());
    }

    @Override
    public User update(User user) {
        int rows = jdbcTemplate.update(
                "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId()
        );
        if (rows == 0) {
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
        return findById(user.getId());
    }

    @Override
    public User findById(int id) {
        List<User> users = jdbcTemplate.query(
                "SELECT user_id, email, login, name, birthday FROM users WHERE user_id = ?",
                mapper,
                id
        );
        if (users.isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        User user = users.get(0);
        loadFriends(user);
        return user;
    }

    @Override
    public void delete(int id) {
        int rows = jdbcTemplate.update("DELETE FROM users WHERE user_id = ?", id);
        if (rows == 0) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
    }

    private void loadFriends(User user) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT friend_id, status_id FROM friendships WHERE user_id = ?",
                user.getId()
        );

        Map<Integer, FriendshipStatus> friends = new HashMap<>();
        for (Map<String, Object> row : rows) {
            int friendId = (Integer) row.get("FRIEND_ID");
            int statusId = (Integer) row.get("STATUS_ID");
            friends.put(friendId, statusId == 2 ? FriendshipStatus.CONFIRMED : FriendshipStatus.UNCONFIRMED);
        }
        user.setFriends(friends);
    }
}