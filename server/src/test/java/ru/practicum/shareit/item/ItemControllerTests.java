package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.ItemRequestController;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemControllerTests {
    @Autowired
    private ItemController itemController;

    @Autowired
    private UserController userController;

    @Autowired
    private BookingController bookingController;

    @Autowired
    private ItemRequestController itemRequestController;

    private ItemDto itemDto;

    private UserDto userDto;

    private ItemRequestDto itemRequestDto;

    private CommentDto comment;

    @BeforeEach
    void init() {
        itemDto = ItemDto.builder()
                .name("name")
                .description("description")
                .available(true)
                .build();

        userDto = UserDto.builder()
                .name("name")
                .email("user@email.com")
                .build();

        itemRequestDto = ItemRequestDto
                .builder()
                .description("item request description")
                .build();

        comment = CommentDto
                .builder()
                .text("first comment")
                .build();
    }

    @Test
    void testCreate() {
        UserDto user = userController.create(userDto);
        ItemDto item = itemController.create(1L, itemDto);
        assertEquals(item.getId(), itemController.getById(item.getId(), user.getId()).getId(), "Ошибка создании вещи");
    }

    @Test
    void testCreateWithRequestTest() {
        UserDto user = userController.create(userDto);
        ItemRequestDto itemRequest = itemRequestController.create(itemRequestDto, user.getId());
        itemDto.setRequestId(1L);
        UserDto user2 = userController.create(userDto.toBuilder().email("user2@email.com").build());
        ItemDto item = itemController.create(2L, itemDto);
        assertEquals(item, itemController.getById(1L, 2L), "Ошибка создании запроса");
    }

    @Test
    void testCreateByWrongUser() {
        assertThrows(NotFoundException.class, () -> itemController.create(1L, itemDto), "Не сработало исключение");
    }

    @Test
    void testCreateWithWrongItemRequest() {
        itemDto.setRequestId(10L);
        UserDto user = userController.create(userDto);
        assertThrows(NotFoundException.class, () -> itemController.create(2L, itemDto),"Не сработало исключение");
    }

    @Test
    void testUpdate() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        ItemDto item = itemDto.toBuilder().name("new name").description("updateDescription").available(false).build();
        itemController.update(item, 1L, 1L);
        assertEquals(item.getDescription(), itemController.getById(1L, 1L).getDescription(), "Ошибка обновления вещи");
    }

    @Test
    void testUpdateForWrongItem() {
        assertThrows(NotFoundException.class, () -> itemController.update(itemDto, 1L, 1L), "Не сработало исключение");
    }

    @Test
    void testUpdateByWrongUser() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        assertThrows(NotFoundException.class, () -> itemController.update(itemDto.toBuilder()
                .name("new name").build(), 1L, 10L), "Не сработало исключение");
    }

    @Test
    void testDelete() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        assertEquals(1, itemController.getAll(1L).size(), "Ошибка удаления");
        itemController.delete(1L);
        assertEquals(0, itemController.getAll(1L).size(), "Ошибка удаления");
    }

    @Test
    void testSearch() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        assertEquals(1, itemController.search("Desc").size(), "Ошибка при поиске");
    }

    @Test
    void testSearchEmptyText() {
        userController.create(userDto);
        itemController.create(1L, itemDto);
        assertEquals(new ArrayList<ItemDto>(), itemController.search(""), "Ошибка при поиске");
    }

    @Test
    void testCreateComment() {
        UserDto user = userController.create(userDto);
        ItemDto item = itemController.create(1L, itemDto);
        UserDto user2 = userController.create(userDto.toBuilder().email("email2@mail.com").build());
        bookingController.create(BookingShortDto.builder()
                .start(LocalDateTime.of(2022, 10, 20, 12, 15))
                .end(LocalDateTime.of(2022, 10, 27, 12, 15))
                .itemId(item.getId()).build(), user2.getId());
        bookingController.approve(1L, 1L, true);
        itemController.createComment(item.getId(), user2.getId(), comment);
        assertEquals(1, itemController.getById(1L, 1L).getComments().size(), "Ошибка при создании комментария");
    }

    @Test
    void testCreateCommentByWrongUser() {
        assertThrows(ValidationException.class, () -> itemController.createComment(1L, 10L, comment), "Не сработало исключение");
    }

    @Test
    void testCreateCommentToWrongItem() {
        UserDto user = userController.create(userDto);
        assertThrows(ValidationException.class, () -> itemController.createComment(1L, 1L, comment), "Не сработало исключение");
        ItemDto item = itemController.create(1L, itemDto);
        assertThrows(ValidationException.class, () -> itemController.createComment(1L, 1L, comment), "Не сработало исключение");
    }

}
