package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.validation.ModelValidator;

import java.util.*;
import java.util.stream.Stream;

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
        normalizeFilm(film);

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
        normalizeFilm(film);

        if (!films.containsKey(film.getId())) {
            log.warn("Film update failed: film not found, id={}", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        Film old = films.get(film.getId());
        if (old.getLikes() != null && (film.getLikes() == null || film.getLikes().isEmpty())) {
            film.setLikes(old.getLikes());
        }

        films.put(film.getId(), film);
        log.info("Film updated: id={}", film.getId());
        return film;
    }

    @GetMapping("/popular")
    public List<Film> getPopular(@RequestParam(defaultValue = "10") int count,
                                 @RequestParam(required = false) Integer genreId,
                                 @RequestParam(required = false) Integer year) {

        Stream<Film> stream = films.values().stream();

        if (genreId != null) {
            stream = stream.filter(f ->
                    f.getGenres() != null && f.getGenres().stream().anyMatch(g -> genreId.equals(g.getId()))
            );
        }

        if (year != null) {
            stream = stream.filter(f ->
                    f.getReleaseDate() != null && f.getReleaseDate().getYear() == year
            );
        }

        return stream
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes() == null ? 0 : f.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    private void normalizeFilm(Film film) {
        if (film.getDescription() == null) {
            film.setDescription("");
        }
        if (film.getMpa() == null) {
            film.setMpa(new Mpa());
        }
        if (film.getMpa().getId() == null) {
            film.getMpa().setId(0);
        }
        if (film.getGenres() == null) {
            film.setGenres(new ArrayList<Genre>());
        }
        if (film.getDirectors() == null) {
            film.setDirectors(new ArrayList<Director>());
        }
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<Integer>());
        }
    }
}