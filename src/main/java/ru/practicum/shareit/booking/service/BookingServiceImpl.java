package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingShortDto;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.UnknownStateException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.BookingMapper.toBooking;
import static ru.practicum.shareit.booking.BookingMapper.toBookingDto;
import static ru.practicum.shareit.enums.Status.*;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final Sort sort = Sort.by(Sort.Direction.DESC, "start");

    @Transactional
    @Override
    public BookingDto create(BookingShortDto bookingShortDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id " + userId));
        Item item = itemRepository.findById(bookingShortDto.getItemId())
                .orElseThrow(() -> new NotFoundException("Не найдена вещь с id " + bookingShortDto.getItemId()));
        if (!item.getAvailable()) {
            throw new ValidationException("Данная вещь недоступна");
        }
        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Пользователь не может забронировать свою вещь");
        }
        Booking booking = toBooking(bookingShortDto);
        if (booking.getEnd().isBefore(booking.getStart()) || booking.getStart().isEqual(booking.getEnd())) {
            throw new ValidationException("Дата окончания бронирования не может быть раньше даты начала бронирования");
        }
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(WAITING);
        bookingRepository.save(booking);

        return toBookingDto(booking);

    }

    @Transactional
    @Override
    public BookingDto approve(Long bookingId, Long userId, Boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Не найдено бронирование с id " + bookingId));
        if (!userId.equals(booking.getItem().getOwner().getId())) {
            throw new NotFoundException("Не найдено бронирование с id " + bookingId + " у пользователя с id" + userId);
        }
        if (!booking.getStatus().equals(WAITING)) {
            throw new ValidationException("Некорректный статус бронирования");
        }
        if (Boolean.TRUE.equals(approved)) {
            booking.setStatus(APPROVED);
        } else {
            booking.setStatus(REJECTED);
        }
        bookingRepository.save(booking);

        return toBookingDto(booking);
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> getAllByOwner(Long userId, String state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не существует пользователя с id " + userId));
        List<Booking> bookingDtoList = new ArrayList<>();
        switch (state) {
            case "ALL":
                bookingDtoList.addAll(bookingRepository.findAllByItemOwner(user, sort));
                break;
            case "CURRENT":
                bookingDtoList.addAll(bookingRepository.findAllByItemOwnerAndStartBeforeAndEndAfter(user,
                        LocalDateTime.now(), LocalDateTime.now(), sort));
                break;
            case "PAST":
                bookingDtoList.addAll(bookingRepository.findAllByItemOwnerAndEndBefore(user,
                        LocalDateTime.now(), sort));
                break;
            case "FUTURE":
                bookingDtoList.addAll(bookingRepository.findAllByItemOwnerAndStartAfter(user, LocalDateTime.now(), sort));
                break;
            case "WAITING":
                bookingDtoList.addAll(bookingRepository.findAllByItemOwnerAndStatusEquals(user, WAITING, sort));
                break;
            case "REJECTED":
                bookingDtoList.addAll(bookingRepository.findAllByItemOwnerAndStatusEquals(user, REJECTED, sort));
                break;
            default:
                throw new UnknownStateException("Unknown state: UNSUPPORTED_STATUS");
        }

        return bookingDtoList.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<BookingDto> getAllByUser(Long userId, String state) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Не найден пользователь с id " + userId));
        List<Booking> bookingDtoList = new ArrayList<>();
        switch (state) {
            case "ALL":
                bookingDtoList.addAll(bookingRepository.findAllByBooker(user, sort));
                break;
            case "CURRENT":
                bookingDtoList.addAll(bookingRepository.findAllByBookerAndStartBeforeAndEndAfter(user,
                        LocalDateTime.now(), LocalDateTime.now(), sort));
                break;
            case "PAST":
                bookingDtoList.addAll(bookingRepository.findAllByBookerAndEndBefore(user,
                        LocalDateTime.now(), sort));
                break;
            case "FUTURE":
                bookingDtoList.addAll(bookingRepository.findAllByBookerAndStartAfter(user, LocalDateTime.now(), sort));
                break;
            case "WAITING":
                bookingDtoList.addAll(bookingRepository.findAllByBookerAndStatusEquals(user, WAITING, sort));
                break;
            case "REJECTED":
                bookingDtoList.addAll(bookingRepository.findAllByBookerAndStatusEquals(user, REJECTED, sort));
                break;
            default:
                throw new UnknownStateException("Unknown state: UNSUPPORTED_STATUS");
        }

        return bookingDtoList.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public BookingDto getById(Long bookingId, Long userId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException("Не существует бронирования с id " + bookingId));
        if (!userId.equals(booking.getBooker().getId()) && !userId.equals(booking.getItem().getOwner().getId())) {
            throw new NotFoundException("Доступ к просмотру бронирования невозможен для текущего пользователя");
        }

        return toBookingDto(booking);
    }
}
