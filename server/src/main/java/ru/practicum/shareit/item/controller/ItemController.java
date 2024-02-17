package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Util.Util;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@RequestHeader(Util.HEADER_USER_ID) Long userId,
                               @PathVariable Long itemId) {
        log.info("ItemController => getItemById: userId={}, itemId={}", userId, itemId);
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getItems(
            @RequestHeader(Util.HEADER_USER_ID) Long userId,
            @RequestParam(value = "from", defaultValue = "0") int from,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("ItemController => getItems: userId={}, from={}, size={}", userId, from, size);
        return itemService.getItems(userId, from, size);
    }

    @PostMapping
    public ItemDto addItem(@RequestBody ItemDto itemDto,
                           @RequestHeader(Util.HEADER_USER_ID) Long ownerId) {
        log.info("ItemController => addItem: ownerId={}, itemDto={}", ownerId, itemDto);
        itemDto.setOwnerId(ownerId);
        return itemService.addItem(itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@RequestBody ItemDto itemDto,
                              @PathVariable Long itemId,
                              @RequestHeader(Util.HEADER_USER_ID) Long ownerId) {
        log.info("ItemController => updateItem: ownerId={}, itemId={}, itemDto={}", ownerId, itemId, itemDto);
        itemDto.setId(itemId);
        itemDto.setOwnerId(ownerId);
        return itemService.updateItem(itemDto);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(
            @RequestParam("text") String text,
            @RequestParam(value = "from", defaultValue = "0") int from,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("ItemController => searchItems: text={}, from={}, size={}", text, from, size);
        return itemService.searchItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto addComment(@RequestBody CommentDto commentDto,
                                  @RequestHeader(Util.HEADER_USER_ID) Long userId,
                                  @PathVariable Long itemId) {
        log.info("ItemController => addComment: userId={}, itemId={}, commentDto={}", userId, itemId, commentDto);
        return itemService.addComment(commentDto, userId, itemId);
    }
}
