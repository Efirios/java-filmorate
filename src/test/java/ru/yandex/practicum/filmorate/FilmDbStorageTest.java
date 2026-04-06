package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, LikeDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final FilmDbStorage filmDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @Test
    void createAndFindById() {
        prepareMpaAndGenres();

        Film film = new Film();
        film.setName("name");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Genre genre = new Genre();
        genre.setId(1);
        film.setGenres(new HashSet<>());
        film.getGenres().add(genre);

        Film created = filmDbStorage.create(film);
        Film found = filmDbStorage.findById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getName()).isEqualTo("name");
        assertThat(found.getMpa().getId()).isEqualTo(1);
        assertThat(found.getMpa().getName()).isEqualTo("G");
        assertThat(found.getGenres()).anyMatch(g -> g.getId().equals(1) && "Комедия".equals(g.getName()));
    }

    @Test
    void findAll() {
        prepareMpaAndGenres();

        Film film1 = new Film();
        film1.setName("name1");
        film1.setDescription("desc1");
        film1.setReleaseDate(LocalDate.of(2000, 1, 1));
        film1.setDuration(100);
        Mpa mpa1 = new Mpa();
        mpa1.setId(1);
        film1.setMpa(mpa1);
        filmDbStorage.create(film1);

        Film film2 = new Film();
        film2.setName("name2");
        film2.setDescription("desc2");
        film2.setReleaseDate(LocalDate.of(2001, 1, 1));
        film2.setDuration(120);
        Mpa mpa2 = new Mpa();
        mpa2.setId(1);
        film2.setMpa(mpa2);
        filmDbStorage.create(film2);

        Collection<Film> films = filmDbStorage.findAll();

        assertThat(films).hasSize(2);
    }

    @Test
    void update() {
        prepareMpaAndGenres();
        jdbcTemplate.update("MERGE INTO mpa (mpa_id, name) KEY(mpa_id) VALUES (2, 'PG')");

        Film film = new Film();
        film.setName("name");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Film created = filmDbStorage.create(film);

        created.setName("newName");
        created.setDescription("newDesc");
        created.setDuration(200);
        Mpa newMpa = new Mpa();
        newMpa.setId(2);
        created.setMpa(newMpa);

        Film updated = filmDbStorage.update(created);

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).isEqualTo("newName");
        assertThat(updated.getDescription()).isEqualTo("newDesc");
        assertThat(updated.getDuration()).isEqualTo(200);
        assertThat(updated.getMpa().getId()).isEqualTo(2);
        assertThat(updated.getMpa().getName()).isEqualTo("PG");
    }

    @Test
    void delete() {
        prepareMpaAndGenres();

        Film film = new Film();
        film.setName("name");
        film.setDescription("desc");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);

        Film created = filmDbStorage.create(film);
        filmDbStorage.delete(created.getId());

        assertThatThrownBy(() -> filmDbStorage.findById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findById_notFound() {
        assertThatThrownBy(() -> filmDbStorage.findById(9999))
                .isInstanceOf(NotFoundException.class);
    }

    private void prepareMpaAndGenres() {
        jdbcTemplate.update("MERGE INTO mpa (mpa_id, name) KEY(mpa_id) VALUES (1, 'G')");
        jdbcTemplate.update("MERGE INTO genres (genre_id, name) KEY(genre_id) VALUES (1, 'Комедия')");
    }
}