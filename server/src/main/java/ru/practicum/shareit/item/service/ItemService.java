package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {

    ItemDto getItemById(Long itemId, Long userId);

    List<ItemDto> getItems(Long userId, int from, int size);

    ItemDto addItem(ItemDto itemDto);

    ItemDto updateItem(ItemDto itemDto);

    List<ItemDto> searchItems(String text, int from, int size);

    CommentDto addComment(CommentDto commentDto, Long userId, Long itemId);
}
