package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {
    private final UserDbStorage userDbStorage;

    @Test
    void createAndFindById() {
        User user = new User();
        user.setEmail("a@b.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userDbStorage.create(user);
        User found = userDbStorage.findById(created.getId());

        assertThat(found.getId()).isEqualTo(created.getId());
        assertThat(found.getEmail()).isEqualTo("a@b.ru");
        assertThat(found.getLogin()).isEqualTo("login");
        assertThat(found.getName()).isEqualTo("name");
    }

    @Test
    void findAll() {
        User user1 = new User();
        user1.setEmail("a@b.ru");
        user1.setLogin("login1");
        user1.setName("name1");
        user1.setBirthday(LocalDate.of(2000, 1, 1));
        userDbStorage.create(user1);

        User user2 = new User();
        user2.setEmail("c@d.ru");
        user2.setLogin("login2");
        user2.setName("name2");
        user2.setBirthday(LocalDate.of(2001, 1, 1));
        userDbStorage.create(user2);

        Collection<User> users = userDbStorage.findAll();

        assertThat(users).hasSize(2);
    }

    @Test
    void update() {
        User user = new User();
        user.setEmail("a@b.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userDbStorage.create(user);

        created.setEmail("new@b.ru");
        created.setLogin("newLogin");
        created.setName("newName");
        created.setBirthday(LocalDate.of(1999, 1, 1));

        User updated = userDbStorage.update(created);

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getEmail()).isEqualTo("new@b.ru");
        assertThat(updated.getLogin()).isEqualTo("newLogin");
        assertThat(updated.getName()).isEqualTo("newName");
        assertThat(updated.getBirthday()).isEqualTo(LocalDate.of(1999, 1, 1));
    }

    @Test
    void delete() {
        User user = new User();
        user.setEmail("a@b.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.of(2000, 1, 1));

        User created = userDbStorage.create(user);
        userDbStorage.delete(created.getId());

        assertThatThrownBy(() -> userDbStorage.findById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findById_notFound() {
        assertThatThrownBy(() -> userDbStorage.findById(9999))
                .isInstanceOf(NotFoundException.class);
    }
}