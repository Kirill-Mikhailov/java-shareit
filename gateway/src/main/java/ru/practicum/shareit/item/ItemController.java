package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.markers.AddItemValidation;
import ru.practicum.shareit.item.dto.markers.UpdateItemValidation;
import ru.practicum.shareit.util.Util;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {

    private final ItemClient itemClient;

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@Positive @RequestHeader(Util.HEADER_USER_ID) Long userId,
                                              @Positive @PathVariable Long itemId) {
        log.info("ItemController => getItemById: userId={}, itemId={}", userId, itemId);
        return itemClient.getItemById(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItems(
            @Positive @RequestHeader(Util.HEADER_USER_ID) Long userId,
            @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
            @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("ItemController => getItems: userId={}, from={}, size={}", userId, from, size);
        return itemClient.getItems(userId, from, size);
    }

    @PostMapping
    public ResponseEntity<Object> addItem(@Validated(AddItemValidation.class) @RequestBody ItemDto itemDto,
                           @Positive @RequestHeader(Util.HEADER_USER_ID) Long ownerId) {
        log.info("ItemController => addItem: ownerId={}, itemDto={}", ownerId, itemDto);
        return itemClient.addItem(ownerId, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@Validated(UpdateItemValidation.class) @RequestBody ItemDto itemDto,
                              @Positive @PathVariable Long itemId,
                              @Positive @RequestHeader(Util.HEADER_USER_ID) Long ownerId) {
        log.info("ItemController => updateItem: ownerId={}, itemId={}, itemDto={}", ownerId, itemId, itemDto);
        return itemClient.updateItem(ownerId, itemId, itemDto);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> searchItems(
            @Positive @RequestHeader(Util.HEADER_USER_ID) Long userId,
            @RequestParam("text") String text,
            @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
            @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("ItemController => searchItems: userId={}, text={}, from={}, size={}", userId, text, from, size);
        return itemClient.searchItems(userId, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@Valid @RequestBody CommentDto commentDto,
                                  @Positive @RequestHeader(Util.HEADER_USER_ID) Long userId,
                                  @Positive @PathVariable Long itemId) {
        log.info("ItemController => addComment: userId={}, itemId={}, commentDto={}", userId, itemId, commentDto);
        return itemClient.addComment(userId, itemId, commentDto);
    }
}
