package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto create(ItemRequestDto itemRequestDto, long userId);

    List<ItemRequestDto> getAllByOwner(long userId);

    List<ItemRequestDto> getAll(long userId, int from, int size);

    ItemRequestDto get(long userId, long requestId);
}
