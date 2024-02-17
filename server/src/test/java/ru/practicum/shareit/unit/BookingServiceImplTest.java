package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.shareit.Util.DateTimeService;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.SendingBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingServiceImpl;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
    @Mock
    private BookingRepository bookingStorage;
    @Mock
    private UserRepository userStorage;
    @Mock
    private ItemRepository itemStorage;
    @Mock
    private DateTimeService dateTimeService;
    @InjectMocks
    private BookingServiceImpl bookingService;

    private User user;
    private User user2;
    private Item item;
    private BookingDto bookingDto;
    private Booking booking;

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

        this.booking = Booking.builder()
                .id(1L)
                .item(item)
                .booker(user2)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .build();

        this.bookingDto = BookingDto.builder()
                .id(1L)
                .bookerId(2L)
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(1L)
                .status(Status.WAITING)
                .build();
    }

    @Test
    public void shouldAddBooking() {
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user2));
        Mockito
                .when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        Mockito
                .when(bookingStorage.save(any(Booking.class)))
                .then(returnsFirstArg());

        SendingBookingDto sendingBookingDto = bookingService.addBooking(this.bookingDto);

        assertThat(sendingBookingDto.getId(), equalTo(bookingDto.getId()));
        assertThat(sendingBookingDto.getStart(), equalTo(bookingDto.getStart()));
        assertThat(sendingBookingDto.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(sendingBookingDto.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(sendingBookingDto.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(sendingBookingDto.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldNotAddBookingWhenUserNotFound() {
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> bookingService.addBooking(bookingDto)
        );

        assertThat(e.getMessage(), equalTo("Пользователя с таким id не существует"));
    }

    @Test
    public void shouldNotAddBookingWhenItemNotFound() {
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user2));
        Mockito
                .when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        ItemNotFoundException e = assertThrows(
                ItemNotFoundException.class,
                () -> bookingService.addBooking(bookingDto)
        );

        assertThat(e.getMessage(), equalTo("Вещи с таким id не существует"));
    }

    @Test
    public void shouldNotAddBookingWhenOwner() {
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user));

        Mockito
                .when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));

        WrongOwnerException e = assertThrows(
                WrongOwnerException.class,
                () -> bookingService.addBooking(bookingDto)
        );

        assertThat(e.getMessage(), equalTo("Владелец вещи не может бронировать свои вещи"));
    }

    @Test
    public void shouldNotAddBookingWhenItemUnavailable() {
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user2));

        item.setAvailable(false);
        Mockito
                .when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));

        NotAvailableForBookingException e = assertThrows(
                NotAvailableForBookingException.class,
                () -> bookingService.addBooking(bookingDto)
        );

        assertThat(e.getMessage(), equalTo("Вещь недоступна для аренды"));
    }

    @Test
    public void shouldNotAddBookingWhenEndBeforeStart() {
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user2));
        Mockito
                .when(itemStorage.findById(anyLong()))
                .thenReturn(Optional.of(item));
        bookingDto.setEnd(bookingDto.getStart().minusDays(1));

        DateNotValidException e = assertThrows(
                DateNotValidException.class,
                () -> bookingService.addBooking(bookingDto)
        );

        assertThat(e.getMessage(), equalTo("Дата начала бронирования не может быть позже даты его окончания"));
    }

    @Test
    public void shouldApproveBooking() {
        Mockito
                .when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        Mockito
                .when(bookingStorage.save(any(Booking.class)))
                .then(returnsFirstArg());

        SendingBookingDto newBookingDto = bookingService.approveBooking(1L, 1L, true);

        assertThat(newBookingDto.getId(), equalTo(booking.getId()));
        assertThat(newBookingDto.getStart(), equalTo(booking.getStart()));
        assertThat(newBookingDto.getEnd(), equalTo(booking.getEnd()));
        assertThat(newBookingDto.getItem().getId(), equalTo(booking.getItem().getId()));
        assertThat(newBookingDto.getBooker().getId(), equalTo(booking.getBooker().getId()));
        assertThat(newBookingDto.getStatus(), equalTo(Status.APPROVED));
    }

    @Test
    public void shouldRejectBooking() {
        Mockito
                .when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.of(booking));
        Mockito
                .when(bookingStorage.save(any(Booking.class)))
                .then(returnsFirstArg());

        SendingBookingDto newBookingDto = bookingService.approveBooking(1L, 1L, false);

        assertThat(newBookingDto.getId(), equalTo(booking.getId()));
        assertThat(newBookingDto.getStart(), equalTo(booking.getStart()));
        assertThat(newBookingDto.getEnd(), equalTo(booking.getEnd()));
        assertThat(newBookingDto.getItem().getId(), equalTo(booking.getItem().getId()));
        assertThat(newBookingDto.getBooker().getId(), equalTo(booking.getBooker().getId()));
        assertThat(newBookingDto.getStatus(), equalTo(Status.REJECTED));
    }

    @Test
    public void shouldNotApproveBookingWhenBookingNotFound() {
        Mockito
                .when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        BookingNotFoundException e = assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.approveBooking(1L, 1L, true)
        );

        assertThat(e.getMessage(), equalTo("Бронирования с таким id не существует"));
        verify(bookingStorage, never()).save(any(Booking.class));
    }

    @Test
    public void shouldNotApproveBookingWhenNotOwner() {
        Mockito
                .when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        WrongOwnerException e = assertThrows(
                WrongOwnerException.class,
                () -> bookingService.approveBooking(2L, 1L, true)
        );

        assertThat(e.getMessage(), equalTo("Подтверждать бронирование может только владелец вещи"));
    }

    @Test
    public void shouldNotApproveBookingWhenStatusIsApproved() {
        booking.setStatus(Status.APPROVED);
        Mockito
                .when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        BookingAlreadyApprovedException e = assertThrows(
                BookingAlreadyApprovedException.class,
                () -> bookingService.approveBooking(2L, 1L, true)
        );

        assertThat(e.getMessage(), equalTo("Бронирование уже подтверждено"));
    }

    @Test
    public void shouldGetById() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        SendingBookingDto sendingBookingDto = bookingService.getBooking(1L, 1L);

        assertThat(sendingBookingDto.getId(), equalTo(bookingDto.getId()));
        assertThat(sendingBookingDto.getStart(), equalTo(bookingDto.getStart()));
        assertThat(sendingBookingDto.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(sendingBookingDto.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(sendingBookingDto.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(sendingBookingDto.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldNotGetByIdWhenUserNotFound() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(false);

        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getBooking(3L, 1L)
        );

        assertThat(e.getMessage(), equalTo("Пользователя с таким id не существует"));
        verify(bookingStorage, never()).findById(anyLong());
    }

    @Test
    public void shouldNotGetByIdWhenBookingNotFound() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        BookingNotFoundException e = assertThrows(
                BookingNotFoundException.class,
                () -> bookingService.getBooking(1L, 3L)
        );

        assertThat(e.getMessage(), equalTo("Бронирования с таким id не существует"));
    }

    @Test
    public void shouldNotGetByIdWhenUserNotOwnerOrBooker() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findById(anyLong()))
                .thenReturn(Optional.of(booking));

        WrongOwnerException e = assertThrows(
                WrongOwnerException.class,
                () -> bookingService.getBooking(3L, 1L)
        );

        assertThat(e.getMessage(), equalTo("Получить данные о бронировании может только его автор и владелец вещи"));
    }

    @Test
    public void shouldGetListOfUsersBookingsWhereStateIsAll() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findBookingsByBookerId(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<SendingBookingDto> bookings = bookingService.getListOfBookingsUserItemsOrUserBookings(2L, "ALL",
                0, 5, false);
        SendingBookingDto sendingBookingDto = bookings.get(0);

        assertThat(bookings.size(), equalTo(1));
        assertThat(sendingBookingDto.getId(), equalTo(bookingDto.getId()));
        assertThat(sendingBookingDto.getStart(), equalTo(bookingDto.getStart()));
        assertThat(sendingBookingDto.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(sendingBookingDto.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(sendingBookingDto.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(sendingBookingDto.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldGetListOfUsersBookingsWhereStateIsCurrent() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findBookingsByBookerIdAndStartBeforeAndEndAfter(anyLong(), any(LocalDateTime.class),
                        any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<SendingBookingDto> bookings = bookingService.getListOfBookingsUserItemsOrUserBookings(2L, "CURRENT",
                0, 5, false);
        SendingBookingDto sendingBookingDto = bookings.get(0);

        assertThat(bookings.size(), equalTo(1));
        assertThat(sendingBookingDto.getId(), equalTo(bookingDto.getId()));
        assertThat(sendingBookingDto.getStart(), equalTo(bookingDto.getStart()));
        assertThat(sendingBookingDto.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(sendingBookingDto.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(sendingBookingDto.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(sendingBookingDto.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldGetListOfUsersBookingsWhereStateIsPast() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findBookingsByBookerIdAndEndBefore(anyLong(), any(LocalDateTime.class),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<SendingBookingDto> bookings = bookingService.getListOfBookingsUserItemsOrUserBookings(2L, "PAST",
                0, 5, false);
        SendingBookingDto sendingBookingDto = bookings.get(0);

        assertThat(bookings.size(), equalTo(1));
        assertThat(sendingBookingDto.getId(), equalTo(bookingDto.getId()));
        assertThat(sendingBookingDto.getStart(), equalTo(bookingDto.getStart()));
        assertThat(sendingBookingDto.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(sendingBookingDto.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(sendingBookingDto.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(sendingBookingDto.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldGetListOfUsersBookingsWhereStateIsFuture() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findBookingsByBookerIdAndStartAfter(anyLong(), any(LocalDateTime.class),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<SendingBookingDto> bookings = bookingService.getListOfBookingsUserItemsOrUserBookings(2L, "FUTURE",
                0, 5, false);
        SendingBookingDto sendingBookingDto = bookings.get(0);

        assertThat(bookings.size(), equalTo(1));
        assertThat(sendingBookingDto.getId(), equalTo(bookingDto.getId()));
        assertThat(sendingBookingDto.getStart(), equalTo(bookingDto.getStart()));
        assertThat(sendingBookingDto.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(sendingBookingDto.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(sendingBookingDto.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(sendingBookingDto.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldGetListOfUsersBookingsWhereStateIsWaiting() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findBookingsByBookerIdAndStatus(anyLong(), any(Status.class),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<SendingBookingDto> bookings = bookingService.getListOfBookingsUserItemsOrUserBookings(2L, "WAITING",
                0, 5, false);
        SendingBookingDto sendingBookingDto = bookings.get(0);

        assertThat(bookings.size(), equalTo(1));
        assertThat(sendingBookingDto.getId(), equalTo(bookingDto.getId()));
        assertThat(sendingBookingDto.getStart(), equalTo(bookingDto.getStart()));
        assertThat(sendingBookingDto.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(sendingBookingDto.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(sendingBookingDto.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(sendingBookingDto.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldGetListOfUsersBookingsWhereStateIsRejected() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findBookingsByBookerIdAndStatus(anyLong(), any(Status.class),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<SendingBookingDto> bookings = bookingService.getListOfBookingsUserItemsOrUserBookings(2L, "REJECTED",
                0, 5, false);
        SendingBookingDto sendingBookingDto = bookings.get(0);

        assertThat(bookings.size(), equalTo(1));
        assertThat(sendingBookingDto.getId(), equalTo(bookingDto.getId()));
        assertThat(sendingBookingDto.getStart(), equalTo(bookingDto.getStart()));
        assertThat(sendingBookingDto.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(sendingBookingDto.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(sendingBookingDto.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(sendingBookingDto.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldGetListOfBookingsUserItemsWhereStateIsAll() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findBookingsByItemOwnerId(anyLong(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<SendingBookingDto> bookings = bookingService.getListOfBookingsUserItemsOrUserBookings(1L, "ALL",
                0, 5, true);
        SendingBookingDto sendingBookingDto = bookings.get(0);

        assertThat(bookings.size(), equalTo(1));
        assertThat(sendingBookingDto.getId(), equalTo(bookingDto.getId()));
        assertThat(sendingBookingDto.getStart(), equalTo(bookingDto.getStart()));
        assertThat(sendingBookingDto.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(sendingBookingDto.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(sendingBookingDto.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(sendingBookingDto.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldGetListOfBookingsUserItemsWhereStateIsCurrent() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findBookingsByItemOwnerIdAndStartBeforeAndEndAfter(anyLong(),
                        any(LocalDateTime.class), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<SendingBookingDto> bookings = bookingService.getListOfBookingsUserItemsOrUserBookings(1L,
                "CURRENT", 0, 5, true);
        SendingBookingDto sendingBookingDto = bookings.get(0);

        assertThat(bookings.size(), equalTo(1));
        assertThat(sendingBookingDto.getId(), equalTo(bookingDto.getId()));
        assertThat(sendingBookingDto.getStart(), equalTo(bookingDto.getStart()));
        assertThat(sendingBookingDto.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(sendingBookingDto.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(sendingBookingDto.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(sendingBookingDto.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldGetListOfBookingsUserItemsWhereStateIsPast() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findBookingsByItemOwnerIdAndEndBefore(anyLong(), any(LocalDateTime.class),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<SendingBookingDto> bookings = bookingService.getListOfBookingsUserItemsOrUserBookings(1L,
                "PAST", 0, 5, true);
        SendingBookingDto sendingBookingDto = bookings.get(0);

        assertThat(bookings.size(), equalTo(1));
        assertThat(sendingBookingDto.getId(), equalTo(bookingDto.getId()));
        assertThat(sendingBookingDto.getStart(), equalTo(bookingDto.getStart()));
        assertThat(sendingBookingDto.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(sendingBookingDto.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(sendingBookingDto.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(sendingBookingDto.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldGetListOfBookingsUserItemsWhereStateIsFuture() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findBookingsByItemOwnerIdAndStartAfter(anyLong(), any(LocalDateTime.class),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<SendingBookingDto> bookings = bookingService.getListOfBookingsUserItemsOrUserBookings(1L,
                "FUTURE", 0, 5, true);
        SendingBookingDto sendingBookingDto = bookings.get(0);

        assertThat(bookings.size(), equalTo(1));
        assertThat(sendingBookingDto.getId(), equalTo(bookingDto.getId()));
        assertThat(sendingBookingDto.getStart(), equalTo(bookingDto.getStart()));
        assertThat(sendingBookingDto.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(sendingBookingDto.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(sendingBookingDto.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(sendingBookingDto.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldGetListOfBookingsUserItemsWhereStateIsWaiting() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findBookingsByItemOwnerIdAndStatus(anyLong(), any(Status.class),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<SendingBookingDto> bookings = bookingService.getListOfBookingsUserItemsOrUserBookings(1L,
                "WAITING", 0, 5, true);
        SendingBookingDto sendingBookingDto = bookings.get(0);

        assertThat(bookings.size(), equalTo(1));
        assertThat(sendingBookingDto.getId(), equalTo(bookingDto.getId()));
        assertThat(sendingBookingDto.getStart(), equalTo(bookingDto.getStart()));
        assertThat(sendingBookingDto.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(sendingBookingDto.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(sendingBookingDto.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(sendingBookingDto.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldGetListOfBookingsUserItemsWhereStateIsRejected() {
        Mockito
                .when(dateTimeService.now())
                .thenReturn(LocalDateTime.now());
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(true);
        Mockito
                .when(bookingStorage.findBookingsByItemOwnerIdAndStatus(anyLong(), any(Status.class),
                        any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));

        List<SendingBookingDto> bookings = bookingService.getListOfBookingsUserItemsOrUserBookings(1L,
                "REJECTED", 0, 5, true);
        SendingBookingDto sendingBookingDto = bookings.get(0);

        assertThat(bookings.size(), equalTo(1));
        assertThat(sendingBookingDto.getId(), equalTo(bookingDto.getId()));
        assertThat(sendingBookingDto.getStart(), equalTo(bookingDto.getStart()));
        assertThat(sendingBookingDto.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(sendingBookingDto.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(sendingBookingDto.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(sendingBookingDto.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldNotGetListOfBookingsUserItemsOrUserBookingsWhenUserNotFound() {
        Mockito
                .when(userStorage.existsById(anyLong()))
                .thenReturn(false);

        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getListOfBookingsUserItemsOrUserBookings(3L,
                        "REJECTED", 0, 5, true)
        );

        assertThat(e.getMessage(), equalTo("Пользователя с таким id не существует"));
    }
}
