package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBooker(User booker, Pageable pageable);

    List<Booking> findAllByBookerAndStartBeforeAndEndAfter(User booker, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findAllByBookerAndEndBefore(User booker, LocalDateTime end, Pageable pageable);

    List<Booking> findAllByBookerAndStartAfter(User booker, LocalDateTime start, Pageable pageable);

    List<Booking> findAllByBookerAndStatusEquals(User booker, Status status, Pageable pageable);

    List<Booking> findAllByItemOwner(User owner, Pageable pageable);

    List<Booking> findAllByItemOwnerAndStartBeforeAndEndAfter(User owner, LocalDateTime start, LocalDateTime end, Pageable pageable);

    List<Booking> findAllByItemOwnerAndEndBefore(User owner, LocalDateTime end, Pageable pageable);

    List<Booking> findAllByItemOwnerAndStartAfter(User owner, LocalDateTime start, Pageable pageable);

    List<Booking> findAllByItemOwnerAndStatusEquals(User owner, Status status, Pageable pageable);

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
