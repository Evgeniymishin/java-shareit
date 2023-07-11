package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBooker(User booker, Sort sort);

    List<Booking> findAllByBookerAndStartBeforeAndEndAfter(User booker, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findAllByBookerAndEndBefore(User booker, LocalDateTime end, Sort sort);

    List<Booking> findAllByBookerAndStartAfter(User booker, LocalDateTime start, Sort sort);

    List<Booking> findAllByBookerAndStatusEquals(User booker, Status status, Sort sort);

    List<Booking> findAllByItemOwner(User owner, Sort sort);

    List<Booking> findAllByItemOwnerAndStartBeforeAndEndAfter(User owner, LocalDateTime start, LocalDateTime end, Sort sort);

    List<Booking> findAllByItemOwnerAndEndBefore(User owner, LocalDateTime end, Sort sort);

    List<Booking> findAllByItemOwnerAndStartAfter(User owner, LocalDateTime start, Sort sort);

    List<Booking> findAllByItemOwnerAndStatusEquals(User owner, Status status, Sort sort);

    List<Booking> findBookingsByBookerIdAndItemIdAndEndIsBeforeAndStatus(long bookerId, long itemId, LocalDateTime localDateTime, Status status);

    @Query(value = "SELECT * FROM bookings b " +
            "WHERE item_id IN (:itemId) " +
            "AND status = 'APPROVED' " +
            "AND ((start_date = " +
            "(SELECT MAX(start_date) " +
            "FROM bookings b " +
            "WHERE item_id IN (:itemId) " +
            "AND start_date <= :now " +
            "AND status = 'APPROVED'" +
            "AND b.item_id = item_id)) " +
            "OR (b.start_date = " +
            "(SELECT MIN(start_date) " +
            "FROM bookings " +
            "WHERE item_id IN (:itemId) " +
            "AND start_date >= :now " +
            "AND status = 'APPROVED' " +
            "AND b.item_id = item_id)))",
            nativeQuery = true)
    List<Booking> getPreviousAndNextBookings(@Param("itemId") List<Long> itemId, @Param("now") LocalDateTime now);
}
