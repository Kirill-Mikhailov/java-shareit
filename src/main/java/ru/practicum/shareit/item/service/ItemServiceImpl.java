package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.WrongOwnerException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemStorage;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemStorage itemStorage;
    private final UserService userService;

    @Override
    public ItemDto getItemById(Long itemId) {
        return ItemMapper.toItemDto(itemStorage.getItemById(itemId));
    }

    @Override
    public List<ItemDto> getItems(Long userId) {
        userService.getUserById(userId);
        return itemStorage.getItems(userId).stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public ItemDto addItem(ItemDto itemDto) {
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(UserMapper.toUser(userService.getUserById(itemDto.getOwnerId())));
        return ItemMapper.toItemDto(itemStorage.addItem(item));
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        ItemDto oldItemDto = getItemById(itemDto.getId());
        if (!oldItemDto.getOwnerId().equals(itemDto.getOwnerId())) {
            throw new WrongOwnerException("Редактировать вщи может только их владелец");
        }
        if (itemDto.getName() == null) itemDto.setName(oldItemDto.getName());
        if (itemDto.getDescription() == null) itemDto.setDescription(oldItemDto.getDescription());
        if (itemDto.getAvailable() == null) itemDto.setAvailable(oldItemDto.getAvailable());
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(UserMapper.toUser(userService.getUserById(itemDto.getOwnerId())));
        return ItemMapper.toItemDto(itemStorage.updateItem(item));
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        return itemStorage.searchItems(text).stream().map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }
}
