package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Util.Util;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Slf4j
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto addItemRequest(@RequestBody ItemRequestDto itemRequestDto,
                                         @RequestHeader(Util.HEADER_USER_ID) Long userId) {
        log.info("ItemRequestController => addItemRequest: userId={}, itemRequestDto={}", userId, itemRequestDto);
        return itemRequestService.addItemRequest(itemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> getItemRequests(@RequestHeader(Util.HEADER_USER_ID) Long userId) {
        log.info("ItemRequestController => getItemRequests: userId={}", userId);
        return itemRequestService.getItemRequests(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllItemRequests(
            @RequestHeader(Util.HEADER_USER_ID) Long userId,
            @RequestParam(value = "from", defaultValue = "0") int from,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("ItemRequestController => getAllItemRequests: userId={}, from={}, size={}", userId, from, size);
        return itemRequestService.getAllItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getItemRequest(@RequestHeader(Util.HEADER_USER_ID) Long userId,
                                         @PathVariable(name = "requestId") Long requestId) {
        log.info("ItemRequestController => getItemRequest: userId={}, requestId={}", userId, requestId);
        return itemRequestService.getItemRequest(userId, requestId);
    }
}
