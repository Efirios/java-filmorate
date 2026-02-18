package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.ModelValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        ModelValidator.validateUser(user);

        user.setId(nextId++);
        users.put(user.getId(), user);

        log.info("User created: id={}, login={}", user.getId(), user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        if (user == null || user.getId() == null) {
            log.warn("User update failed: id is missing");
            throw new ValidationException("Id должен быть указан");
        }

        ModelValidator.validateUser(user);

        if (!users.containsKey(user.getId())) {
            log.warn("User update failed: user not found, id={}", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }

        users.put(user.getId(), user);
        log.info("User updated: id={}", user.getId());
        return user;
    }
}