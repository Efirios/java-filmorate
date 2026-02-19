package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.validation.ModelValidator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        ModelValidator.validateFilm(film);

        film.setId(nextId++);
        films.put(film.getId(), film);

        log.info("Film created: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        if (film == null || film.getId() == null) {
            log.warn("Film update failed: id is missing");
            throw new ValidationException("Id должен быть указан");
        }

        ModelValidator.validateFilm(film);

        if (!films.containsKey(film.getId())) {
            log.warn("Film update failed: film not found, id={}", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        films.put(film.getId(), film);
        log.info("Film updated: id={}", film.getId());
        return film;
    }
}