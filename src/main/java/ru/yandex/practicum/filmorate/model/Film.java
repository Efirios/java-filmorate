package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class Film {
    private Integer id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Integer duration;

    private Mpa mpa = new Mpa();
    private List<Genre> genres = new ArrayList<>();
    private List<Director> directors = new ArrayList<>();

    private Set<Integer> likes = new HashSet<>();
}