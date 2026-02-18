package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.validation.ModelValidator;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ModelValidatorTest {

    @Test
    void filmDescription200_ok_201_fail() {
        Film ok = new Film();
        ok.setName("A");
        ok.setDescription("a".repeat(200));
        ok.setReleaseDate(LocalDate.of(2000, 1, 1));
        ok.setDuration(1);
        assertDoesNotThrow(() -> ModelValidator.validateFilm(ok));

        Film bad = new Film();
        bad.setName("A");
        bad.setDescription("a".repeat(201));
        bad.setReleaseDate(LocalDate.of(2000, 1, 1));
        bad.setDuration(1);
        assertThrows(ValidationException.class, () -> ModelValidator.validateFilm(bad));
    }

    @Test
    void filmReleaseDateBoundary() {
        Film ok = new Film();
        ok.setName("A");
        ok.setDescription("d");
        ok.setReleaseDate(LocalDate.of(1895, 12, 28));
        ok.setDuration(1);
        assertDoesNotThrow(() -> ModelValidator.validateFilm(ok));

        Film bad = new Film();
        bad.setName("A");
        bad.setDescription("d");
        bad.setReleaseDate(LocalDate.of(1895, 12, 27));
        bad.setDuration(1);
        assertThrows(ValidationException.class, () -> ModelValidator.validateFilm(bad));
    }

    @Test
    void userBlankName_shouldBecomeLogin() {
        User u = new User();
        u.setEmail("a@b.ru");
        u.setLogin("login");
        u.setName("   ");
        u.setBirthday(LocalDate.of(2000, 1, 1));

        ModelValidator.validateUser(u);
        assertEquals("login", u.getName());
    }

    @Test
    void userFutureBirthday_fail() {
        User u = new User();
        u.setEmail("a@b.ru");
        u.setLogin("login");
        u.setName("name");
        u.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> ModelValidator.validateUser(u));
    }
}