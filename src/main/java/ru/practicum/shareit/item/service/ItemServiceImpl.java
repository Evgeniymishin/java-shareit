package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.booking.BookingMapper.toBookingShortDto;
import static ru.practicum.shareit.item.ItemMapper.toItem;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ItemDto> getAll(Long userId) {
        List<Item> items = itemRepository.findAllByOwnerId(userId);
        List<ItemDto> itemDtoList = items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        itemDtoList.forEach(itemDto -> {
            itemDto.setLastBooking(bookingRepository.findAllByItemIdOrderByStartAsc(itemDto.getId()).isEmpty() ?
                    null : toBookingShortDto(bookingRepository.findAllByItemIdOrderByStartAsc(itemDto.getId()).get(0)));
            itemDto.setNextBooking(itemDto.getLastBooking() == null ?
                    null : toBookingShortDto(bookingRepository.findAllByItemIdOrderByStartDesc(itemDto.getId()).get(0)));
        });


        return itemDtoList;
    }

    @Transactional(readOnly = true)
    @Override
    public ItemDto getById(Long id, Long ownerId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Не найдена вещь с id: " + id));
        ItemDto itemDto = toItemDto(item);
        if (item.getOwner().getId().equals(ownerId)) {
            itemDto.setLastBooking(bookingRepository.findAllByItemIdOrderByStartAsc(id).isEmpty() ? null :
                    toBookingShortDto(bookingRepository.findAllByItemIdOrderByStartAsc(id).get(0)));
            itemDto.setNextBooking(itemDto.getLastBooking() == null ?
                    null : toBookingShortDto(bookingRepository.findAllByItemIdOrderByStartDesc(itemDto.getId())
                    .get(0)));
        }
        return toItemDto(item);
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
}
