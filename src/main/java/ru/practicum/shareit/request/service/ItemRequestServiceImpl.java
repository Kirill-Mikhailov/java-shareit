package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.Util.DateTimeService;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestStorage;
    private final UserRepository userStorage;
    private final DateTimeService dateTimeService;
    private final ItemRepository itemStorage;

    @Override
    public ItemRequestDto addItemRequest(ItemRequestDto itemRequestDto, Long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователя с таким id не существует"));
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequestor(user);
        itemRequest.setCreated(dateTimeService.now());
        return ItemRequestMapper.toItemRequestDto(itemRequestStorage.save(itemRequest));
    }

    @Override
    public List<ItemRequestDto> getItemRequests(Long userId) {
        if (!userStorage.existsById(userId)) throw new UserNotFoundException("Пользователя с таким id не существует");
        return itemRequestStorage.findItemRequestsByRequestorIdOrderByCreatedDesc(userId)
                .stream().map(ItemRequestMapper::toItemRequestDto)
                .peek(itemRequestDto ->
                        itemRequestDto.setItems(
                                itemStorage.findItemsByItemRequestId(itemRequestDto.getId()).stream()
                                        .map(ItemMapper::toItemDtoForRequestDto).collect(Collectors.toList())
                        )
                ).collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllItemRequests(Long userId, int from, int size) {
        if (!userStorage.existsById(userId)) throw new UserNotFoundException("Пользователя с таким id не существует");
        int page = from / size;
        return itemRequestStorage.findItemRequestsByRequestorIdNot(userId,
                PageRequest.of(page, size, Sort.by("created").descending()))
                .stream().map(ItemRequestMapper::toItemRequestDto)
                .peek(itemRequestDto ->
                        itemRequestDto.setItems(
                                itemStorage.findItemsByItemRequestId(itemRequestDto.getId()).stream()
                                        .map(ItemMapper::toItemDtoForRequestDto).collect(Collectors.toList())
                        )
                ).collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getItemRequest(Long userId, Long requestId) {
        if (!userStorage.existsById(userId)) throw new UserNotFoundException("Пользователя с таким id не существует");
        ItemRequest itemRequest = itemRequestStorage.findById(requestId)
                .orElseThrow(() -> new ItemRequestNotFoundException("Запроса с таким id не существует"));
        ItemRequestDto itemRequestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        itemRequestDto.setItems(itemStorage.findItemsByItemRequestId(itemRequestDto.getId())
                        .stream().map(ItemMapper::toItemDtoForRequestDto).collect(Collectors.toList())
        );
        return itemRequestDto;
    }

}
