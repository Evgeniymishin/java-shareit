package ru.practicum.shareit.request;

import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
public class ItemRequestController {
    private final ItemRequestServiceImpl ItemRequestService;

    public ItemRequestController(ItemRequestServiceImpl ItemRequestService) {
        this.ItemRequestService = ItemRequestService;
    }

    @PostMapping
    ItemRequestDto create(@Valid @RequestBody ItemRequestDto itemRequestDto,
                          @RequestHeader("X-Sharer-User-Id") long userId) {
        return ItemRequestService.create(itemRequestDto, userId);
    }

    @GetMapping("/{requestId}")
    ItemRequestDto get(@RequestHeader("X-Sharer-User-Id") long userId,
                       @PathVariable long requestId) {
        return ItemRequestService.get(userId, requestId);
    }

    @GetMapping("/all")
    List<ItemRequestDto> getAll(@RequestHeader("X-Sharer-User-Id") long userId,
                                @RequestParam(defaultValue = "0") int from,
                                @RequestParam(defaultValue = "10") int size) {
        if (from < 0 || size <= 0) {
            throw new ValidationException("Неккоректные параметры запроса");
        }
        return ItemRequestService.getAll(userId, from, size);
    }

    @GetMapping()
    List<ItemRequestDto> getAllByOwner(@RequestHeader("X-Sharer-User-Id") long userId) {
        return ItemRequestService.getAllByOwner(userId);
    }
}
