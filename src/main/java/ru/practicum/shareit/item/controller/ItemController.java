package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Util.Util;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.item.dto.markers.AddItemValidation;
import ru.practicum.shareit.item.dto.markers.UpdateItemValidation;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/{itemId}")
    public ItemDto getItemById(@Valid @Positive @RequestHeader(Util.HEADER_USER_ID) Long userId,
                               @Valid @Positive @PathVariable Long itemId) {
        return itemService.getItemById(itemId, userId);
    }

    @GetMapping
    public List<ItemDto> getItems(
            @Valid @Positive @RequestHeader(Util.HEADER_USER_ID) Long userId,
            @Valid @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
            @Valid @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        return itemService.getItems(userId, from, size);
    }

    @PostMapping()
    public ItemDto addItem(@Validated(AddItemValidation.class) @RequestBody ItemDto itemDto,
                           @Valid @Positive @RequestHeader(Util.HEADER_USER_ID) Long ownerId) {
        itemDto.setOwnerId(ownerId);
        return itemService.addItem(itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto updateItem(@Validated(UpdateItemValidation.class) @RequestBody ItemDto itemDto,
                              @Valid @Positive @PathVariable Long itemId,
                              @Valid @Positive @RequestHeader(Util.HEADER_USER_ID) Long ownerId) {
        itemDto.setId(itemId);
        itemDto.setOwnerId(ownerId);
        return itemService.updateItem(itemDto);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItems(
            @RequestParam("text") String text,
            @Valid @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
            @Valid @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        return itemService.searchItems(text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    private CommentDto addComment(@Validated @RequestBody CommentDto commentDto,
                                  @Valid @Positive @RequestHeader(Util.HEADER_USER_ID) Long userId,
                                  @Valid @Positive @PathVariable Long itemId) {
        return itemService.addComment(commentDto, userId, itemId);
    }
}
