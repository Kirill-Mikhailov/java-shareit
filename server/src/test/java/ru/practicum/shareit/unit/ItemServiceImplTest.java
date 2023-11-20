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
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CommentMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemServiceImpl;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @Mock
    private ItemRepository itemStorage;
    @Mock
    private UserRepository userStorage;
    @Mock
    private BookingRepository bookingStorage;
    @Mock
    private CommentRepository commentStorage;
    @Mock
    private ItemRequestRepository itemRequestStorage;
    @Mock
    private DateTimeService dateTimeService;
    @InjectMocks
    private ItemServiceImpl itemServiceImpl;
    private Item item;
    private User user;
    private User user2;
    private Comment comment;
    private Booking booking;
    private Booking booking2;
    private ItemRequest itemRequest;

    @BeforeEach
    public void beforeEach() {
        this.user = User.builder()
                .id(1L)
                .name("name")
                .email("email")
                .build();
        this.user2 = User.builder()
                .id(2L)
                .name("name2")
                .email("email2")
                .build();
        this.item = Item.builder()
                .id(1L)
                .name("itemName")
                .description("description")
                .available(true)
                .owner(user)
                .build();
        this.comment = Comment.builder()
                .id(1L)
                .item(item)
                .author(user)
                .text("text")
                .build();
        this.booking = Booking.builder()
                .id(1L)
                .item(item)
                .booker(user2)
                .status(Status.APPROVED)
                .start(LocalDateTime.of(2023, 11, 9, 13, 30))
                .end(LocalDateTime.of(2023, 11, 10, 13, 30))
                .build();
        this.booking2 = Booking.builder()
                .id(2L)
                .item(item)
                .booker(user2)
                .status(Status.APPROVED)
                .start(LocalDateTime.of(2023, 11, 12, 13, 30))
                .end(LocalDateTime.of(2023, 11, 13, 13, 30))
                .build();
        this.itemRequest = ItemRequest.builder()
                .id(1L)
                .description("description")
                .requestor(user2)
                .build();
    }

    @Test
    void shouldGetItemByIdForOtherUserTest() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.of(2023, 11, 11, 13, 30));
        comment.setCreated(dateTimeService.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Mockito
                .when(commentStorage.findCommentsByItemIdOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(comment));

        ItemDto itemDto = itemServiceImpl.getItemById(1L, user2.getId());

        assertThat(itemDto.getId(), equalTo(item.getId()));
        assertThat(itemDto.getName(), equalTo(item.getName()));
        assertThat(itemDto.getDescription(), equalTo(item.getDescription()));
        assertThat(itemDto.getAvailable(), equalTo(item.getAvailable()));
        assertThat(itemDto.getOwnerId(), equalTo(user.getId()));
        assertThat(itemDto.getComments().size(), equalTo(1));
    }

    @Test
    void shouldGetItemByIdForOwnerTest() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.of(2023, 11, 11, 13, 30));
        comment.setCreated(dateTimeService.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Mockito
                .when(commentStorage.findCommentsByItemIdOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(comment));
        Mockito
                .when(bookingStorage.findTopBookingByItemIdAndStatusNotAndStartBeforeOrderByEndDesc(1L,
                        Status.REJECTED, dateTimeService.now()))
                .thenReturn(booking);
        Mockito
                .when(bookingStorage.findTopBookingByItemIdAndStatusNotAndStartAfterOrderByStartAsc(1L,
                        Status.REJECTED, dateTimeService.now()))
                .thenReturn(booking2);

        ItemDto itemDto = itemServiceImpl.getItemById(1L, user.getId());

        assertThat(itemDto.getId(), equalTo(item.getId()));
        assertThat(itemDto.getName(), equalTo(item.getName()));
        assertThat(itemDto.getDescription(), equalTo(item.getDescription()));
        assertThat(itemDto.getAvailable(), equalTo(item.getAvailable()));
        assertThat(itemDto.getOwnerId(), equalTo(user.getId()));
        assertThat(itemDto.getComments().size(), equalTo(1));
        assertThat(itemDto.getLastBooking(), equalTo(BookingMapper.toBookingDto(booking)));
        assertThat(itemDto.getNextBooking(), equalTo(BookingMapper.toBookingDto(booking2)));
    }

    @Test
    void shouldNotGetItemByIdWhenUserNotFoundTest() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(false);

        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> itemServiceImpl.getItemById(1L, user.getId())
        );

        assertThat(e.getMessage(), equalTo("Пользователя с таким id не существует"));
        verify(itemStorage, never()).findById(anyLong());
    }

    @Test
    void shouldNotGetItemByIdWhenItemNotFoundTest() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        ItemNotFoundException e = assertThrows(
                ItemNotFoundException.class,
                () -> itemServiceImpl.getItemById(1L, user.getId())
        );

        assertThat(e.getMessage(), equalTo("Вещи с таким id не существует"));
    }

    @Test
    void shouldGetItemsTest() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.of(2023, 11, 11, 13, 30));
        comment.setCreated(dateTimeService.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(itemStorage.findItemsByOwnerId(anyLong(), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item)));
        Mockito
                .when(bookingStorage.findTopBookingByItemIdAndStatusNotAndStartBeforeOrderByEndDesc(
                        anyLong(),
                        ArgumentMatchers.any(Status.class),
                        ArgumentMatchers.any(LocalDateTime.class)
                ))
                .thenReturn(booking);
        Mockito
                .when(bookingStorage.findTopBookingByItemIdAndStatusNotAndStartAfterOrderByStartAsc(
                        anyLong(),
                        ArgumentMatchers.any(Status.class),
                        ArgumentMatchers.any(LocalDateTime.class)
                ))
                .thenReturn(booking2);
        Mockito
                .when(commentStorage.findCommentsByItemIdOrderByCreatedDesc(anyLong()))
                .thenReturn(List.of(comment));

        List<ItemDto> items = itemServiceImpl.getItems(1L, 0, 10);
        ItemDto itemDto = items.get(0);

        assertThat(items.size(), equalTo(1));
        assertThat(itemDto.getId(), equalTo(item.getId()));
        assertThat(itemDto.getName(), equalTo(item.getName()));
        assertThat(itemDto.getDescription(), equalTo(item.getDescription()));
        assertThat(itemDto.getAvailable(), equalTo(item.getAvailable()));
        assertThat(itemDto.getLastBooking(), equalTo(BookingMapper.toBookingDto(booking)));
        assertThat(itemDto.getNextBooking(), equalTo(BookingMapper.toBookingDto(booking2)));
        assertThat(itemDto.getComments().size(), equalTo(1));
        assertThat(itemDto.getComments().get(0).getId(), equalTo(comment.getId()));
    }

    @Test
    void shouldNotGetItemsWhenUserNotFoundTest() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(false);

        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> itemServiceImpl.getItems(1L, 0, 10)
        );

        assertThat(e.getMessage(), equalTo("Пользователя с таким id не существует"));
        verify(itemStorage, never()).findItemsByOwnerId(anyLong(), ArgumentMatchers.any(Pageable.class));
    }

    @Test
    void shouldAddItemTest() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.of(2023, 11, 11, 13, 30));
        itemRequest.setCreated(dateTimeService.now());
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemStorage.save(ArgumentMatchers.any(Item.class)))
                .thenReturn(item);

        ItemDto itemDto = itemServiceImpl.addItem(ItemMapper.toItemDto(item, null));

        assertThat(itemDto.getId(), equalTo(item.getId()));
        assertThat(itemDto.getName(), equalTo(item.getName()));
        assertThat(itemDto.getDescription(), equalTo(item.getDescription()));
        assertThat(itemDto.getAvailable(), equalTo(item.getAvailable()));
        assertThat(itemDto.getLastBooking(), nullValue());
        assertThat(itemDto.getNextBooking(), nullValue());
        assertThat(itemDto.getComments(), nullValue());
    }

    @Test
    void shouldAddItemForRequestTest() {
        item.setItemRequest(itemRequest);
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.of(2023, 11, 11, 13, 30));
        itemRequest.setCreated(dateTimeService.now());
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestStorage.findById(anyLong()))
                .thenReturn(Optional.of(itemRequest));
        Mockito
                .when(itemStorage.save(ArgumentMatchers.any(Item.class)))
                .thenReturn(item);

        ItemDto itemDto = itemServiceImpl.addItem(ItemMapper.toItemDto(item, null));

        assertThat(itemDto.getId(), equalTo(item.getId()));
        assertThat(itemDto.getName(), equalTo(item.getName()));
        assertThat(itemDto.getDescription(), equalTo(item.getDescription()));
        assertThat(itemDto.getAvailable(), equalTo(item.getAvailable()));
        assertThat(itemDto.getRequestId(), equalTo(item.getItemRequest().getId()));
        assertThat(itemDto.getLastBooking(), nullValue());
        assertThat(itemDto.getNextBooking(), nullValue());
        assertThat(itemDto.getComments(), nullValue());
    }

    @Test
    void shouldNotAddItemWhenUserNotFoundTest() {
        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> itemServiceImpl.addItem(ItemMapper.toItemDto(item, null))
        );

        assertThat(e.getMessage(), equalTo("Пользователя с таким id не существует"));
        verify(itemStorage, never()).save(ArgumentMatchers.any(Item.class));
    }

    @Test
    void shouldNotAddItemWhenItemRequestNotFoundTest() {
        item.setItemRequest(itemRequest);
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.of(2023, 11, 11, 13, 30));
        itemRequest.setCreated(dateTimeService.now());
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemRequestStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        ItemRequestNotFoundException e = assertThrows(
                ItemRequestNotFoundException.class,
                () -> itemServiceImpl.addItem(ItemMapper.toItemDto(item, null))
        );

        assertThat(e.getMessage(), equalTo("Запроса вещи с таким id не существует"));
        verify(itemStorage, never()).save(ArgumentMatchers.any(Item.class));
    }

    @Test
    public void shouldUpdateItemTest() {
        Mockito
                .when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Mockito
                .when(itemStorage.save(ArgumentMatchers.any(Item.class)))
                .then(returnsFirstArg());

        ItemDto itemDto = itemServiceImpl.updateItem(ItemMapper.toItemDto(item, null));

        assertThat(itemDto.getId(), equalTo(item.getId()));
        assertThat(itemDto.getName(), equalTo(item.getName()));
        assertThat(itemDto.getDescription(), equalTo(item.getDescription()));
        assertThat(itemDto.getAvailable(), equalTo(item.getAvailable()));
        assertThat(itemDto.getLastBooking(), nullValue());
        assertThat(itemDto.getNextBooking(), nullValue());
        assertThat(itemDto.getComments(), nullValue());
    }

    @Test
    public void shouldNotUpdateItemWhenItemNotFoundTest() {
        Mockito
                .when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        ItemNotFoundException e = assertThrows(
                ItemNotFoundException.class,
                () -> itemServiceImpl.updateItem(ItemMapper.toItemDto(item, null))
        );

        assertThat(e.getMessage(), equalTo("Вещи с таким id не существует"));
        verify(itemStorage, never()).save(ArgumentMatchers.any(Item.class));
    }

    @Test
    public void shouldNotUpdateItemWhenUserNotOwnerTest() {
        Item item2 = Item.builder()
                .id(1L)
                .name("itemName2")
                .description("description2")
                .available(true)
                .owner(user2)
                .build();
        Mockito
                .when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        WrongOwnerException e = assertThrows(
                WrongOwnerException.class,
                () -> itemServiceImpl.updateItem(ItemMapper.toItemDto(item2, null))
        );

        assertThat(e.getMessage(), equalTo("Редактировать вещи может только их владелец"));
    }


    @Test
    public void shouldSearchItemsTest() {
        Mockito
                .when(itemStorage.searchItems(ArgumentMatchers.any(String.class), ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(item)));

        List<ItemDto> items = itemServiceImpl.searchItems("Test", 0, 5);
        ItemDto itemDto = items.get(0);

        assertThat(itemDto.getId(), equalTo(item.getId()));
        assertThat(itemDto.getName(), equalTo(item.getName()));
        assertThat(itemDto.getDescription(), equalTo(item.getDescription()));
        assertThat(itemDto.getAvailable(), equalTo(item.getAvailable()));
        assertThat(itemDto.getLastBooking(), nullValue());
        assertThat(itemDto.getNextBooking(), nullValue());
        assertThat(itemDto.getComments(), nullValue());
    }

    @Test
    public void shouldSearchEmptyListWhenTextIsBlankTest() {
        List<ItemDto> items = itemServiceImpl.searchItems("", 0, 5);

        assertTrue(items.isEmpty());
    }

    @Test
    public void shouldAddCommentTest() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.of(2023, 11, 11, 13, 30));
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Mockito
                .when(bookingStorage.existsBookingByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(),
                                ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(true);
        Mockito
                .when(commentStorage.save(ArgumentMatchers.any(Comment.class)))
                .thenReturn(comment);

        CommentDto commentDto = itemServiceImpl.addComment(CommentMapper.toCommentDto(comment), 2L, 1L);


        assertThat(commentDto.getId(), equalTo(comment.getId()));
        assertThat(commentDto.getText(), equalTo(comment.getText()));
        assertThat(commentDto.getAuthorName(), equalTo(comment.getAuthor().getName()));
        assertThat(commentDto.getCreated(), equalTo(comment.getCreated()));
    }

    @Test
    public void shouldNotAddCommentWhenUserNotFoundTest() {
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> itemServiceImpl.addComment(CommentMapper.toCommentDto(comment), 2L, 1L)
        );

        assertThat(e.getMessage(), equalTo("Пользователя с таким id не существует"));
    }

    @Test
    public void shouldNotAddCommentWhenItemNotFoundTest() {
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        ItemNotFoundException e = assertThrows(
                ItemNotFoundException.class,
                () -> itemServiceImpl.addComment(CommentMapper.toCommentDto(comment), 2L, 1L)
        );

        assertThat(e.getMessage(), equalTo("Вещи с таким id не существует"));
    }

    @Test
    public void shouldNotAddCommentWhenBookingNotFoundTest() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.of(2023, 12, 11, 13, 30));
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Mockito
                .when(bookingStorage.existsBookingByBookerIdAndItemIdAndEndBefore(anyLong(), anyLong(),
                                ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(false);

        UserNotValidException e = assertThrows(
                UserNotValidException.class,
                () -> itemServiceImpl.addComment(CommentMapper.toCommentDto(comment), 2L, 1L)
        );

        assertThat(e.getMessage(), equalTo("Отзыв может оставить только арендатор вещи после завершения аренды"));
    }

}