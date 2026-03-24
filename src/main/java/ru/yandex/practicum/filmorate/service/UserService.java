package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.ModelValidator;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
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
        User user = userStorage.findById(id);
        User friend = userStorage.findById(friendId);

        FriendshipStatus reverseStatus = friend.getFriends().get(id);

        if (reverseStatus != null) {
            user.getFriends().put(friendId, FriendshipStatus.CONFIRMED);
            friend.getFriends().put(id, FriendshipStatus.CONFIRMED);
        } else {
            user.getFriends().put(friendId, FriendshipStatus.UNCONFIRMED);
        }

        log.info("Friend added: userId={}, friendId={}", id, friendId);
    }

    public void removeFriend(int id, int friendId) {
        User user = userStorage.findById(id);
        User friend = userStorage.findById(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        log.info("Friend removed: userId={}, friendId={}", id, friendId);
    }

    public List<User> getFriends(int id) {
        User user = userStorage.findById(id);
        return user.getFriends().entrySet().stream()
                .filter(e -> e.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int id, int otherId) {
        User user = userStorage.findById(id);
        User other = userStorage.findById(otherId);

        Set<Integer> userFriends = user.getFriends().entrySet().stream()
                .filter(e -> e.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        Set<Integer> otherFriends = other.getFriends().entrySet().stream()
                .filter(e -> e.getValue() == FriendshipStatus.CONFIRMED)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(userStorage::findById)
                .collect(Collectors.toList());
    }
}