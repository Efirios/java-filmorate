package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.List;

@Component
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Mpa> mapper = (rs, rowNum) -> {
        Mpa mpa = new Mpa();
        mpa.setId(rs.getInt("mpa_id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    };

    @Override
    public Collection<Mpa> findAll() {
        return jdbcTemplate.query("SELECT mpa_id, name FROM mpa ORDER BY mpa_id", mapper);
    }

    @Override
    public Mpa findById(int id) {
        List<Mpa> mpas = jdbcTemplate.query(
                "SELECT mpa_id, name FROM mpa WHERE mpa_id = ?",
                mapper,
                id
        );
        if (mpas.isEmpty()) {
            throw new NotFoundException("MPA с id=" + id + " не найден");
        }
        return mpas.get(0);
    }
}