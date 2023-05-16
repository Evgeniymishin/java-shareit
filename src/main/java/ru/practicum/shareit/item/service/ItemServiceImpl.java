package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static ru.practicum.shareit.item.ItemMapper.toItem;
import static ru.practicum.shareit.item.ItemMapper.toItemDto;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemServiceImpl implements ItemService {
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public List<ItemDto> getAll(Long userId) {
        List<ItemDto> items = new ArrayList<>();
        for (Item item : itemRepository.findAll()) {
            if (item.getOwner().getId().equals(userId)) {
                items.add(toItemDto(item));
            }
        }
        return items;
    }

    @Override
    public ItemDto getById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Не найдена вещь с id: " + id));

        return toItemDto(item);
    }

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

    @Override
    public ItemDto update(ItemDto itemDto, Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Не найдена вещь с id: " + id));
        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Ошибка обновления вещи: у пользователя " + userId + "нет такой вещи");
        }
        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return toItemDto(itemRepository.save(item));
    }

    @Override
    public void delete(Long id) {
        getById(id);
        itemRepository.deleteById(id);
    }

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
