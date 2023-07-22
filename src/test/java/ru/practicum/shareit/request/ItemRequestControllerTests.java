package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRequestControllerTests {
    @Autowired
    private ItemRequestController itemRequestController;

    @Autowired
    private UserController userController;

    private ItemRequestDto itemRequestDto;

    private UserDto userDto;

    @BeforeEach
    void init() {
        itemRequestDto = ItemRequestDto
                .builder()
                .description("item request description")
                .build();

        userDto = UserDto
                .builder()
                .name("name")
                .email("user@email.com")
                .build();
    }

    @Test
    void testCreate() {
        UserDto user = userController.create(userDto);
        ItemRequestDto itemRequest = itemRequestController.create(itemRequestDto, user.getId());
        assertEquals(1L, itemRequestController.get(itemRequest.getId(), user.getId()).getId(), "Ошибка создании запроса");
    }

    @Test
    void testCreateByWrongUser() {
        assertThrows(NotFoundException.class, () -> itemRequestController.create(itemRequestDto,1L), "Не сработало исключение");
    }

    @Test
    void testGetAllByUser() {
        UserDto user = userController.create(userDto);
        ItemRequestDto itemRequest = itemRequestController.create(itemRequestDto, user.getId());
        assertEquals(1, itemRequestController.getAllByOwner(user.getId()).size(), "Ошибка получение списка запросов");
    }

    @Test
    void testGetAllByUserWithWrongUser() {
        assertThrows(NotFoundException.class, () -> itemRequestController.getAllByOwner(1L), "Не сработало исключение");
    }

    @Test
    void testGetAll() {
        UserDto user = userController.create(userDto);
        ItemRequestDto itemRequest = itemRequestController.create(itemRequestDto, user.getId());
        assertEquals(0, itemRequestController.getAll(user.getId(), 0, 10).size(), "Ошибка получении пользователей");
        UserDto user2 = userController.create(userDto.toBuilder().email("user1@email.com").build());
        assertEquals(1, itemRequestController.getAll(user2.getId(), 0, 10).size(), "Ошибка получении пользователей");
    }

    @Test
    void testGetAllByWrongUser() {
        assertThrows(NotFoundException.class, () -> itemRequestController.getAll(1L, 0, 10), "Не сработало исключение");
    }

    @Test
    void testGetAllWithWrongFrom() {
        assertThrows(ValidationException.class, () -> itemRequestController.getAll(1L, -1, 10), "Не сработало исключение");
    }
}
