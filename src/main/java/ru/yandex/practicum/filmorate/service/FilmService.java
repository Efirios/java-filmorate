package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.ModelValidator;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LikeDbStorage likeDbStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       LikeDbStorage likeDbStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.likeDbStorage = likeDbStorage;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(int id) {
        return filmStorage.findById(id);
    }

    public Film create(Film film) {
        ModelValidator.validateFilm(film);
        Film created = filmStorage.create(film);
        log.info("Film created: id={}, name={}", created.getId(), created.getName());
        return created;
    }

    public Film update(Film film) {
        if (film == null || film.getId() == null) {
            throw new ValidationException("Id должен быть указан");
        }
        ModelValidator.validateFilm(film);
        Film updated = filmStorage.update(film);
        log.info("Film updated: id={}", updated.getId());
        return updated;
    }

    public void addLike(int filmId, int userId) {
        userStorage.findById(userId);
        filmStorage.findById(filmId);
        likeDbStorage.addLike(filmId, userId);
        log.info("Like added: filmId={}, userId={}", filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        userStorage.findById(userId);
        filmStorage.findById(filmId);
        likeDbStorage.removeLike(filmId, userId);
        log.info("Like removed: filmId={}, userId={}", filmId, userId);
    }

    public List<Film> getPopular(int count) {
        if (count <= 0) {
            throw new ValidationException("count должен быть больше нуля");
        }
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}