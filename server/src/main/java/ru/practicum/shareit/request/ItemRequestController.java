package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;

import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestServiceImpl service;

    public ItemRequestController(ItemRequestServiceImpl service) {
        this.service = service;
    }

    @PostMapping
    public ItemRequestDto create(@RequestBody ItemRequestDto itemRequestDto,
                                 @RequestHeader("X-Sharer-User-Id") long userId) {
        return service.create(itemRequestDto, userId);
    }

    @GetMapping("/{requestId}")
    ItemRequestDto get(@RequestHeader("X-Sharer-User-Id") long userId,
                       @PathVariable long requestId) {
        return service.get(userId, requestId);
    }

    @GetMapping("/all")
    List<ItemRequestDto> getAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                @RequestParam(defaultValue = "0") int from,
                                @RequestParam(defaultValue = "10") int size) {
        return service.getAll(userId, from, size);
    }

    @GetMapping()
    List<ItemRequestDto> getAllByOwner(@RequestHeader("X-Sharer-User-Id") long userId) {
        return service.getAllByOwner(userId);
    }
}
