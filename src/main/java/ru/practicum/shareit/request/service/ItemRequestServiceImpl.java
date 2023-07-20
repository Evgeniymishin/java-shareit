package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto create(ItemRequestDto itemRequestDto, long userId) {
        return ItemRequestMapper.toDto(itemRequestRepository.save(ItemRequestMapper.toItemRequest(itemRequestDto,
                userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Не существует пользователя с id " + userId)))));
    }

    @Override
    public ItemRequestDto get(long userId, long requestId) {
        if (!userRepository.existsById(userId)) throw new NotFoundException("Не существует пользователя с id " + userId);
        return setItemsToRequests(Map.of(requestId, ItemRequestMapper.toDto(itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Не найден запрос с id " + requestId))))).get(0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getAll(long userId, int from, int size) {
        if (!userRepository.existsById(userId)) throw new NotFoundException("Не существует пользователя с id " + userId);
        return setItemsToRequests(itemRequestRepository.findItemRequestsByRequestorIdNotOrderByCreatedDesc(userId, PageRequest.of(from / size, size))
                .stream().map(ItemRequestMapper::toDto).collect(Collectors.toList()).stream().collect(Collectors.toMap(k -> k.getId(), v -> v)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItemRequestDto> getAllByOwner(long userId) {
        if (!userRepository.existsById(userId)) throw new NotFoundException("Не существует пользователя с id " + userId);
        return setItemsToRequests(itemRequestRepository.findItemRequestByRequestorId(userId, Sort.by(Sort.Direction.DESC, "created"))
                .stream().map(ItemRequestMapper::toDto).collect(Collectors.toList()).stream().collect(Collectors.toMap(k -> k.getId(), v -> v)));
    }

    private List<ItemRequestDto> setItemsToRequests(Map<Long, ItemRequestDto> itemRequests) {
        List<ItemDto> items = itemRepository.findAllByRequestIdInOrderByIdDesc(itemRequests.values().stream().map(ItemRequestDto::getId).collect(Collectors.toList()))
                .stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
        for (ItemDto itemDto : items) {
            itemRequests.get(itemDto.getRequestId()).addItemDto(itemDto);
        }
        return itemRequests.values().stream().collect(Collectors.toList());
    }

}
