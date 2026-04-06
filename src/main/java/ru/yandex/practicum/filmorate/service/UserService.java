package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.ModelValidator;

import java.util.List;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipDbStorage friendshipDbStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage,
                       FriendshipDbStorage friendshipDbStorage) {
        this.userStorage = userStorage;
        this.friendshipDbStorage = friendshipDbStorage;
    }

    public java.util.Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(int id) {
        return userStorage.findById(id);
    }

    public User create(User user) {
        ModelValidator.validateUser(user);
        User created = userStorage.create(user);
        log.info("User created: id={}, login={}", created.getId(), created.getLogin());
        return created;
    }

    public User update(User user) {
        if (user == null || user.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        ModelValidator.validateUser(user);
        User updated = userStorage.update(user);
        log.info("User updated: id={}", updated.getId());
        return updated;
    }

    public void addFriend(int id, int friendId) {
        userStorage.findById(id);
        userStorage.findById(friendId);
        friendshipDbStorage.addFriend(id, friendId);
        log.info("Friend added: userId={}, friendId={}", id, friendId);
    }

    public void removeFriend(int id, int friendId) {
        userStorage.findById(id);
        userStorage.findById(friendId);
        friendshipDbStorage.removeFriend(id, friendId);
        log.info("Friend removed: userId={}, friendId={}", id, friendId);
    }

    public List<User> getFriends(int id) {
        userStorage.findById(id);
        return friendshipDbStorage.findFriends(id);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        userStorage.findById(id);
        userStorage.findById(otherId);
        return friendshipDbStorage.findCommonFriends(id, otherId);
    }
}