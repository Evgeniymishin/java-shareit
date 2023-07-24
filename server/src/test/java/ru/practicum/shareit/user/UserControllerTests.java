package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTests {
    @Autowired
    private UserController userController;

    private UserDto user;

    @BeforeEach
    void init() {
        user = UserDto.builder()
                .name("name")
                .email("user@email.com")
                .build();
    }

    @Test
    void testCreate() {
        UserDto userDto = userController.create(user);
        assertEquals(userDto.getId(), userController.getById(userDto.getId()).getId());
    }

    @Test
    void testUpdate() {
        userController.create(user);
        UserDto userDto = user.toBuilder().name("update name").email("update@email.com").build();
        userController.update(userDto, 1L);
        assertEquals(userDto.getEmail(), userController.getById(1L).getEmail());
    }

    @Test
    void testUpdateByWrongUser() {
        assertThrows(NotFoundException.class, () -> userController.update(user, 1L));
    }

    @Test
    void testDelete() {
        UserDto userDto = userController.create(user);
        assertEquals(1, userController.getAll().size(), "Ошибка создания пользователя");
        userController.delete(userDto.getId());
        assertEquals(0, userController.getAll().size(), "Ошибка удаления пользователя");
    }

    @Test
    void testGetByWrongId() {
        assertThrows(NotFoundException.class, () -> userController.getById(1L), "Не сработало исключение");
    }
}