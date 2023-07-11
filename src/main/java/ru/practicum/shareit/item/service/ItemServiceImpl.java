package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.comment.CommentMapper;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.comment.model.Comment;
import ru.practicum.shareit.comment.repository.CommentRepository;
import ru.practicum.shareit.enums.Status;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.item.ItemMapper.toItem;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> getAll(Long userId) {
        Map<Long, ItemDto> items = itemRepository.findAllByOwnerId(userId).stream().collect(Collectors.toMap(Item::getId, ItemMapper::toItemDto));
        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = bookingRepository.getPreviousAndNextBookings(new ArrayList<>(items.keySet()), now);
        for (Booking booking : bookings) {
            if (booking.getStart().isBefore(now)) {
                items.get(booking.getItem().getId()).setLastBooking(BookingMapper.toBookingShortDto(booking));
            } else {
                items.get(booking.getItem().getId()).setNextBooking(BookingMapper.toBookingShortDto(booking));
            }
        }
        return setComments(items, commentRepository.findCommentsByItemId(new ArrayList<>(items.keySet())));
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDto getById(Long id, Long ownerId) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Не найдена вещь с id: " + id));
        if (!userRepository.existsById(ownerId)) throw new NotFoundException("Не найдена пользователь с id: " + id);
        ItemDto itemDto = ItemMapper.toItemDto(item);
        if (ownerId == item.getOwner().getId()) {
            LocalDateTime now = LocalDateTime.now();
            List<Booking> bookings = bookingRepository.getPreviousAndNextBookings(List.of(id), now);
            for (Booking booking : bookings) {
                if (booking.getStart().isBefore(now)) {
                    itemDto.setLastBooking(BookingMapper.toBookingShortDto(booking));
                } else {
                    itemDto.setNextBooking(BookingMapper.toBookingShortDto(booking));
                }
            }
        }
        return setComments(Map.of(itemDto.getId(), itemDto), commentRepository.findCommentsByItemId(new ArrayList<>(List.of(item.getId())))).get(0);
    }

    @Transactional
    @Override
    public ItemDto create(ItemDto itemDto, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Ошибка создания вещи - " +
                        "не найден пользователь с id: " + userId));
        Item item = toItem(itemDto);
        item.setOwner(user);
        itemRepository.save(item);
        return toItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto update(ItemDto itemDto, Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Не найдена вещь с id: " + id));
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Ошибка обновления вещи: у пользователя " + userId + "нет такой вещи");
        }
        Optional.ofNullable(itemDto.getName()).ifPresent(item::setName);
        Optional.ofNullable(itemDto.getDescription()).ifPresent(item::setDescription);
        Optional.ofNullable(itemDto.getAvailable()).ifPresent(item::setAvailable);
        return toItemDto(itemRepository.save(item));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        itemRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> search(String text) {
        List<ItemDto> searchedItems = new ArrayList<>();
        if (text.isBlank()) {
            return searchedItems;
        }
        for (Item item : itemRepository.findAll()) {
            if (item.getName().toLowerCase().contains(text.toLowerCase()) ||
                    item.getDescription().toLowerCase().contains(text.toLowerCase()) && item.getAvailable()) {
                searchedItems.add(toItemDto(item));
            }
        }
        return searchedItems;

    }

    @Transactional
    @Override
    public CommentDto createComment(Long itemId, Long userId, CommentDto commentDto) {
        if (!bookingRepository.findBookingsByBookerIdAndItemIdAndEndIsBeforeAndStatus(userId, itemId, LocalDateTime.now(), Status.APPROVED).isEmpty()) {
            return CommentMapper.toCommentDto(commentRepository.save(CommentMapper.toComment(commentDto,
                    userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Не найден пользователь " + userId)),
                    itemRepository.findById(itemId).orElseThrow(() -> new NotFoundException("Не найдена вещь " + itemId)))));
        } else {
            throw new ValidationException("Вы не можете оставить комментарий к этой вещи");
        }
    }

    private List<ItemDto> setComments(Map<Long, ItemDto> items, List<Comment> comments) {
        for (Comment comment : comments) {
            items.get(comment.getItem().getId()).createComment(CommentMapper.toCommentDto(comment));
        }
        return new ArrayList<>(items.values());
    }

}
