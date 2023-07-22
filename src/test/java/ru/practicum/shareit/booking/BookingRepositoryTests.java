package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingRepositoryTests {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User user;

    private Item item;

    private User user2;

    private Booking booking;

    @BeforeEach
    void init() {
        user = User.builder()
                .name("name")
                .email("email@email.com")
                .build();

        item = Item.builder()
                .name("name")
                .description("description")
                .available(true)
                .owner(user)
                .build();

        user2 = User.builder()
                .name("name2")
                .email("email2@email.com")
                .build();

        booking = Booking.builder()
                .start(LocalDateTime.of(2023, 1, 10, 10, 30))
                .end(LocalDateTime.of(2023, 2, 10, 10, 30))
                .item(item)
                .booker(user2)
                .status(Status.WAITING)
                .build();
    }

    @Test
    void testFindAllByBooker() {
        userRepository.save(user);
        itemRepository.save(item);
        userRepository.save(user2);
        bookingRepository.save(booking);
        assertThat(bookingRepository.findAllByBooker(user2, Pageable.ofSize(10)).stream().count(), equalTo(1L));
    }

    @Test
    void testFindAllByItemOwnerAndEndBefore() {
        userRepository.save(user);
        itemRepository.save(item);
        userRepository.save(user2);
        bookingRepository.save(booking);
        assertThat(bookingRepository.findAllByItemOwnerAndEndBefore(user,
                LocalDateTime.of(2023, 4, 10, 10, 10),
                Pageable.ofSize(10)).stream().count(), equalTo(1L));
    }

    @Test
    void testFindAllByItemOwnerAndStatusEquals() {
        userRepository.save(user);
        itemRepository.save(item);
        userRepository.save(user2);
        bookingRepository.save(booking);
        assertThat(bookingRepository.findAllByItemOwnerAndStatusEquals(user, Status.WAITING, Pageable.ofSize(10))
                .stream().count(), equalTo(1L));
    }
}
