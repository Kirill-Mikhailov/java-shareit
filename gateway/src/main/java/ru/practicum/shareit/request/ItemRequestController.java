package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.util.Util;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/requests")
@Slf4j
@Validated
public class ItemRequestController {

    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> addItemRequest(@Valid @RequestBody ItemRequestDto itemRequestDto,
                                                 @Positive @RequestHeader(Util.HEADER_USER_ID) Long userId) {
        log.info("ItemRequestController => addItemRequest: userId={}, itemRequestDto={}", userId, itemRequestDto);
        return itemRequestClient.addItemRequest(userId, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getItemRequests(@Positive @RequestHeader(Util.HEADER_USER_ID) Long userId) {
        log.info("ItemRequestController => getItemRequests: userId={}", userId);
        return itemRequestClient.getItemRequests(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllItemRequests(
            @Positive @RequestHeader(Util.HEADER_USER_ID) Long userId,
            @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
            @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("ItemRequestController => getAllItemRequests: userId={}, from={}, size={}", userId, from, size);
        return itemRequestClient.getAllItemRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getItemRequest(@Positive @RequestHeader(Util.HEADER_USER_ID) Long userId,
                                         @Positive @PathVariable(name = "requestId") Long requestId) {
        log.info("ItemRequestController => getItemRequest: userId={}, requestId={}", userId, requestId);
        return itemRequestClient.getItemRequest(userId, requestId);
    }
}
