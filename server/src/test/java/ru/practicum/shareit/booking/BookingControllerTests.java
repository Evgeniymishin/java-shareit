package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.UserController;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingControllerTests {
    @Autowired
    private BookingController bookingController;

    @Autowired
    private UserController userController;

    @Autowired
    private ItemController itemController;

    private ItemDto itemDto;

    private UserDto userDto;

    private UserDto userDto1;

    private BookingShortDto bookingShortDto;

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

        userDto1 = UserDto.builder()
                .name("name")
                .email("user1@email.com")
                .build();

        bookingShortDto = BookingShortDto.builder()
                .start(LocalDateTime.of(2022, 10, 24, 12, 30))
                .end(LocalDateTime.of(2023, 11, 10, 13, 0))
                .itemId(1L).build();
    }

    @Test
    void testCreate() {
        UserDto user = userController.create(userDto);
        ItemDto item = itemController.create(user.getId(), itemDto);
        UserDto user1 = userController.create(userDto1);
        BookingDto booking = bookingController.create(bookingShortDto, user1.getId());
        assertEquals(1L, bookingController.getById(booking.getId(), user1.getId()).getId(), "Ошибка создании брони");
    }

    @Test
    void testCreateWrongUser() {
        assertThrows(NotFoundException.class, () -> bookingController.create(bookingShortDto, 1L), "Не сработало исключение");
    }

    @Test
    void testCreateWrongItem() {
        UserDto user = userController.create(userDto);
        assertThrows(NotFoundException.class, () -> bookingController.create(bookingShortDto, 1L), "Не сработало исключение");
    }

    @Test
    void testCreateOwner() {
        UserDto user = userController.create(userDto);
        ItemDto item = itemController.create(user.getId(), itemDto);
        assertThrows(NotFoundException.class, () -> bookingController.create(bookingShortDto, 1L), "Не сработало исключение");
    }

    @Test
    void testCreateToUnavailableItem() {
        UserDto user = userController.create(userDto);
        itemDto.setAvailable(false);
        ItemDto item = itemController.create(user.getId(), itemDto);
        UserDto user1 = userController.create(userDto1);
        assertThrows(ValidationException.class, () -> bookingController.create(bookingShortDto, 2L), "Не сработало исключение");
    }

    @Test
    void testCreateWithWrongEndDate() {
        UserDto user = userController.create(userDto);
        ItemDto item = itemController.create(user.getId(), itemDto);
        UserDto user1 = userController.create(userDto1);
        bookingShortDto.setEnd(LocalDateTime.of(2022, 9, 24, 12, 30));
        assertThrows(ValidationException.class, () -> bookingController.create(bookingShortDto, user1.getId()), "Не сработало исключение");
    }

    @Test
    void testApprove() {
        UserDto user = userController.create(userDto);
        ItemDto item = itemController.create(user.getId(), itemDto);
        UserDto user1 = userController.create(userDto1);
        BookingDto booking = bookingController.create(BookingShortDto.builder()
                .start(LocalDateTime.of(2022, 10, 24, 12, 30))
                .end(LocalDateTime.of(2022, 11, 10, 13, 0))
                .itemId(item.getId()).build(), user1.getId());
        assertEquals(Status.WAITING, bookingController.getById(booking.getId(), user1.getId()).getStatus(), "Ошибочный статус брони");
        bookingController.approve(booking.getId(), user.getId(), true);
        assertEquals(Status.APPROVED, bookingController.getById(booking.getId(), user1.getId()).getStatus(), "Ошибочный статус брони");
    }

    @Test
    void testApproveToWrongBooking() {
        assertThrows(NotFoundException.class, () -> bookingController.approve(1L, 1L, true), "Не сработало исключение");
    }

    @Test
    void testApproveWrongUser() {
        UserDto user = userController.create(userDto);
        ItemDto item = itemController.create(user.getId(), itemDto);
        UserDto user1 = userController.create(userDto1);
        BookingDto booking = bookingController.create(bookingShortDto, user1.getId());
        assertThrows(NotFoundException.class, () -> bookingController.approve(1L, 2L, true), "Не сработало исключение");
    }

    @Test
    void testApproveToBookingWithWrongStatus() {
        UserDto user = userController.create(userDto);
        ItemDto item = itemController.create(user.getId(), itemDto);
        UserDto user1 = userController.create(userDto1);
        BookingDto booking = bookingController.create(bookingShortDto, user1.getId());
        bookingController.approve(1L, 1L, true);
        assertThrows(ValidationException.class, () -> bookingController.approve(1L, 1L, true), "Не сработало исключение");
    }

    @Test
    void testGetAllByUser() {
        UserDto user = userController.create(userDto);
        ItemDto item = itemController.create(user.getId(), itemDto);
        UserDto user1 = userController.create(userDto1);
        BookingDto booking = bookingController.create(bookingShortDto, user1.getId());
        assertEquals(1, bookingController.getAllByUser(user1.getId(), "WAITING", 0, 10).size());
        assertEquals(1, bookingController.getAllByUser(user1.getId(), "ALL", 0, 10).size());
        assertEquals(0, bookingController.getAllByUser(user1.getId(), "PAST", 0, 10).size());
        assertEquals(1, bookingController.getAllByUser(user1.getId(), "CURRENT", 0, 10).size());
        assertEquals(0, bookingController.getAllByUser(user1.getId(), "FUTURE", 0, 10).size());
        assertEquals(0, bookingController.getAllByUser(user1.getId(), "REJECTED", 0, 10).size());
        bookingController.approve(booking.getId(), user.getId(), true);
        assertEquals(1, bookingController.getAllByOwner(user.getId(), "CURRENT", 0, 10).size());
        assertEquals(1, bookingController.getAllByOwner(user.getId(), "ALL", 0, 10).size());
        assertEquals(0, bookingController.getAllByOwner(user.getId(), "WAITING", 0, 10).size());
        assertEquals(0, bookingController.getAllByOwner(user.getId(), "FUTURE", 0, 10).size());
        assertEquals(0, bookingController.getAllByOwner(user.getId(), "REJECTED", 0, 10).size());
        assertEquals(0, bookingController.getAllByOwner(user.getId(), "PAST", 0, 10).size());
    }

    @Test
    void testGetAllByWrongUser() {
        assertThrows(NotFoundException.class, () -> bookingController.getAllByUser(1L, "ALL", 0, 10), "Не сработало исключение");
        assertThrows(NotFoundException.class, () -> bookingController.getAllByOwner(1L, "ALL", 0, 10), "Не сработало исключение");
    }

    @Test
    void testGetByWrongId() {
        assertThrows(NotFoundException.class, () -> bookingController.getById(1L, 1L), "Не сработало исключение");
    }

    @Test
    void testGetByWrongUser() {
        UserDto user = userController.create(userDto);
        ItemDto item = itemController.create(user.getId(), itemDto);
        UserDto user1 = userController.create(userDto1);
        BookingDto booking = bookingController.create(bookingShortDto, user1.getId());
        assertThrows(NotFoundException.class, () -> bookingController.getById(1L, 10L), "Не сработало исключение");
    }
}