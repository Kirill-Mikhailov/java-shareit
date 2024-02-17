package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.Util.DateTimeService;
import ru.practicum.shareit.exception.ItemRequestNotFoundException;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.ItemRequestServiceImpl;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyLong;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceImplTest {
    @Mock
    private ItemRequestRepository itemRequestStorage;
    @Mock
    private UserRepository userStorage;
    @Mock
    private ItemRepository itemStorage;
    @Mock
    private DateTimeService dateTimeService;
    @InjectMocks
    private ItemRequestServiceImpl itemRequestService;

    private User user;
    private ItemRequestDto itemRequestDto;
    private ItemRequest itemRequest;

    @BeforeEach
    public void setUp() {
        this.user = User.builder()
                .id(1L)
                .name("name")
                .email("email")
                .build();

        this.itemRequestDto = ItemRequestDto.builder()
                .id(1L)
                .description("description")
                .created(null)
                .items(Collections.emptyList())
                .build();

        this.itemRequest = ItemRequest.builder()
                .id(1L)
                .description("description")
                .created(null)
                .requestor(user)
                .build();
    }

    @Test
    public void shouldAddItemRequest() {
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.now());
        Mockito
                .when(itemRequestStorage.save(ArgumentMatchers.any(ItemRequest.class)))
                .then(returnsFirstArg());

        ItemRequestDto newItemRequestDto = itemRequestService.addItemRequest(itemRequestDto, 1L);

        assertThat(newItemRequestDto.getId(), equalTo(itemRequestDto.getId()));
        assertThat(newItemRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    public void shouldNotAddItemRequestWhenUserNotFound() {
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> itemRequestService.addItemRequest(itemRequestDto, 2L)
        );

        assertThat(e.getMessage(), equalTo("Пользователя с таким id не существует"));
    }

    @Test
    public void shouldGetItemRequests() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(itemRequestStorage.findItemRequestsByRequestorIdOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(itemRequest));
        Mockito
                .when(itemStorage.findItemsByItemRequestId(anyLong()))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> itemRequests = itemRequestService.getItemRequests(1L);
        ItemRequestDto itemRequestDtoOutgoing = itemRequests.get(0);

        assertThat(itemRequests.size(), equalTo(1));
        assertThat(itemRequestDtoOutgoing.getId(), equalTo(itemRequestDto.getId()));
        assertThat(itemRequestDtoOutgoing.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(itemRequestDtoOutgoing.getItems(), equalTo(itemRequestDto.getItems()));
    }

    @Test
    public void shouldNotGetItemRequestsByUserIdWhenUserNotFound() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(false);

        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> itemRequestService.getItemRequests(1L)
        );

        assertThat(e.getMessage(), equalTo("Пользователя с таким id не существует"));
    }

    @Test
    public void shouldGetAllItemRequests() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(itemRequestStorage.findItemRequestsByRequestorIdNot(anyLong(), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(itemRequest)));
        Mockito
                .when(itemStorage.findItemsByItemRequestId(anyLong()))
                .thenReturn(Collections.emptyList());

        List<ItemRequestDto> itemRequests = itemRequestService.getAllItemRequests(1L, 0, 5);
        ItemRequestDto itemRequestDtoOutgoing = itemRequests.get(0);

        assertThat(itemRequests.size(), equalTo(1));
        assertThat(itemRequestDtoOutgoing.getId(), equalTo(itemRequestDto.getId()));
        assertThat(itemRequestDtoOutgoing.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(itemRequestDtoOutgoing.getItems(), equalTo(itemRequestDto.getItems()));
    }

    @Test
    public void shouldNotGetAllItemRequestsWhenUserNotFound() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(false);

        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> itemRequestService.getAllItemRequests(1L, 0, 5)
        );

        assertThat(e.getMessage(), equalTo("Пользователя с таким id не существует"));
    }

    @Test
    public void shouldGetItemRequest() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(itemRequestStorage.findById(anyLong()))
                .thenReturn(Optional.of(itemRequest));
        Mockito
                .when(itemStorage.findItemsByItemRequestId(anyLong()))
                .thenReturn(Collections.emptyList());

        ItemRequestDto itemRequestDtoOutgoing = itemRequestService.getItemRequest(1L,1L);

        assertThat(itemRequestDtoOutgoing.getId(), equalTo(itemRequestDto.getId()));
        assertThat(itemRequestDtoOutgoing.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(itemRequestDtoOutgoing.getItems(), equalTo(itemRequestDto.getItems()));
    }

    @Test
    public void shouldNotGetItemRequestWhenUserNotFound() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(false);

        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> itemRequestService.getItemRequest(1L,1L)
        );

        assertThat(e.getMessage(), equalTo("Пользователя с таким id не существует"));
    }

    @Test
    public void shouldNotGetItemRequestWhenItemRequestNotFound() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(itemRequestStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        ItemRequestNotFoundException e = assertThrows(
                ItemRequestNotFoundException.class,
                () -> itemRequestService.getItemRequest(1L,1L)
        );

        assertThat(e.getMessage(), equalTo("Запроса с таким id не существует"));
    }
}