package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemStorage;
    private final UserRepository userStorage;
    private final BookingRepository bookingStorage;
    private final CommentRepository commentStorage;

    @Override
    public ItemDto getItemById(Long itemId, Long userId) {
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещи с таким id не существует"));
        ItemDto itemDto = ItemMapper.toItemDto(item,
                commentStorage.findCommentsByItemIdOrderByCreatedDesc(itemId).stream()
                        .map(CommentMapper::toCommentDto).collect(Collectors.toList())
        );
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            itemDto.setLastBooking(BookingMapper.toBookingDto(bookingStorage
                    .findTopBookingByItemIdAndStatusNotAndStartBeforeOrderByEndDesc(itemDto.getId(),
                            Status.REJECTED, now)));
            itemDto.setNextBooking(BookingMapper.toBookingDto(bookingStorage
                    .findTopBookingByItemIdAndStatusNotAndStartAfterOrderByStartAsc(itemDto.getId(),
                            Status.REJECTED, now)));
        }
        return itemDto;
    }

    @Override
    public List<ItemDto> getItems(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return itemStorage.findItemsByOwnerId(userId).stream()
                .map(item -> ItemMapper.toItemDto(
                                item,
                                commentStorage.findCommentsByItemIdOrderByCreatedDesc(item.getId()).stream()
                                        .map(CommentMapper::toCommentDto).collect(Collectors.toList())
                        ))
                .peek(itemDto -> {
                itemDto.setLastBooking(BookingMapper.toBookingDto(bookingStorage
                        .findTopBookingByItemIdAndStatusNotAndStartBeforeOrderByEndDesc(itemDto.getId(),
                                Status.REJECTED, now)));
                itemDto.setNextBooking(BookingMapper.toBookingDto(bookingStorage
                        .findTopBookingByItemIdAndStatusNotAndStartAfterOrderByStartAsc(itemDto.getId(),
                                Status.REJECTED, now)));
                }).collect(Collectors.toList());
    }

    @Override
    public ItemDto addItem(ItemDto itemDto) {
        User user = userStorage.findById(itemDto.getOwnerId())
                .orElseThrow(() -> new UserNotFoundException("Пользователя с таким id не существует"));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(user);
        return ItemMapper.toItemDto(itemStorage.save(item), null);
    }

    @Override
    public ItemDto updateItem(ItemDto itemDto) {
        Item itemFromDB = itemStorage.findById(itemDto.getId())
                .orElseThrow(() -> new ItemNotFoundException("Вещи с таким id не существует"));
        if (!itemFromDB.getOwner().getId().equals(itemDto.getOwnerId())) {
            throw new WrongOwnerException("Редактировать вещи может только их владелец");
        }
        if (Objects.nonNull(itemDto.getName())) itemFromDB.setName(itemDto.getName());
        if (Objects.nonNull(itemDto.getDescription())) itemFromDB.setDescription(itemDto.getDescription());
        if (Objects.nonNull(itemDto.getAvailable())) itemFromDB.setAvailable(itemDto.getAvailable());
        return ItemMapper.toItemDto(itemStorage.save(itemFromDB), null);
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return itemStorage.searchItems(text).stream().map(item -> ItemMapper.toItemDto(item, null))
                .collect(Collectors.toList());
    }

    @Override
    public CommentDto addComment(CommentDto commentDto, Long userId, Long itemId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователя с таким id не существует"));
        Item item = itemStorage.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException("Вещи с таким id не существует"));
        if (!bookingStorage.existsBookingByBookerIdAndItemIdAndEndBefore(userId, itemId, LocalDateTime.now())) {
            throw new UserNotValidException("Отзыв может оставить только арендатор вещи после завершения аренды");
        }
        return CommentMapper.toCommentDto(commentStorage.save(CommentMapper.toComment(commentDto, user, item)));
    }
}
