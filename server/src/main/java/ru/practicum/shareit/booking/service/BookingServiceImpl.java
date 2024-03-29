package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.Util.DateTimeService;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.booking.dto.SendingBookingDto;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.*;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingStorage;
    private final UserRepository userStorage;
    private final ItemRepository itemStorage;
    private final DateTimeService dateTimeService;

    @Override
    public SendingBookingDto addBooking(BookingDto bookingDto) {
        User booker = userStorage.findById(bookingDto.getBookerId())
                .orElseThrow(() -> new UserNotFoundException("Пользователя с таким id не существует"));
        Item item = itemStorage.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ItemNotFoundException("Вещи с таким id не существует"));
        if (bookingDto.getStart().isAfter(bookingDto.getEnd()) || bookingDto.getStart().equals(bookingDto.getEnd())) {
            throw new DateNotValidException("Дата начала бронирования не может быть позже даты его окончания");
        }
        if (item.getOwner().getId().equals(booker.getId())) {
            throw new WrongOwnerException("Владелец вещи не может бронировать свои вещи");
        }
        if (!item.getAvailable()) throw new NotAvailableForBookingException("Вещь недоступна для аренды");
        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setItem(item);
        booking.setBooker(booker);
        return BookingMapper.toExtendedBookingDto(bookingStorage.save(booking));
    }

    @Override
    public SendingBookingDto approveBooking(Long ownerId, Long bookingId, Boolean approved) {
        Booking booking = bookingStorage.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирования с таким id не существует"));
        if (booking.getStatus().equals(Status.APPROVED)) {
            throw new BookingAlreadyApprovedException("Бронирование уже подтверждено");
        }
        User owner = booking.getItem().getOwner();
        if (!owner.getId().equals(ownerId)) {
            throw new WrongOwnerException("Подтверждать бронирование может только владелец вещи");
        }
        if (approved.equals(true)) {
            booking.setStatus(Status.APPROVED);
        } else {
            booking.setStatus(Status.REJECTED);
        }
        return BookingMapper.toExtendedBookingDto(bookingStorage.save(booking));
    }

    @Override
    public SendingBookingDto getBooking(Long userId, Long bookingId) {
        if (!userStorage.existsById(userId)) throw new UserNotFoundException("Пользователя с таким id не существует");
        Booking booking = bookingStorage.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Бронирования с таким id не существует"));
        if (!(userId.equals(booking.getBooker().getId()) || userId.equals(booking.getItem().getOwner().getId()))) {
            throw new WrongOwnerException("Получить данные о бронировании может только его автор и владелец вещи");
        }
        return BookingMapper.toExtendedBookingDto(booking);
    }

    public List<SendingBookingDto> getListOfBookingsUserItemsOrUserBookings(Long userId, String state, int from,
                                                                            int size, boolean isOwner) {
        if (!userStorage.existsById(userId)) throw new UserNotFoundException("Пользователя с таким id не существует");
        int page = from / size;
        Pageable pageable = PageRequest.of(page, size, Sort.by("Start").descending());
        Page<Booking> bookings;
        LocalDateTime now = dateTimeService.now();
        switch (state) {
            case "CURRENT":
                if (isOwner) {
                    bookings = bookingStorage.findBookingsByItemOwnerIdAndStartBeforeAndEndAfter(userId, now, now,
                            pageable);
                } else {
                    bookings = bookingStorage.findBookingsByBookerIdAndStartBeforeAndEndAfter(userId, now, now,
                            pageable);
                }
                break;
            case "PAST":
                if (isOwner) {
                    bookings = bookingStorage.findBookingsByItemOwnerIdAndEndBefore(userId, now, pageable);
                } else {
                    bookings = bookingStorage.findBookingsByBookerIdAndEndBefore(userId, now, pageable);
                }
                break;
            case "FUTURE":
                if (isOwner) {
                    bookings = bookingStorage.findBookingsByItemOwnerIdAndStartAfter(userId, now, pageable);
                } else {
                    bookings = bookingStorage.findBookingsByBookerIdAndStartAfter(userId, now, pageable);
                }
                break;
            case "WAITING":
                if (isOwner) {
                    bookings = bookingStorage.findBookingsByItemOwnerIdAndStatus(userId, Status.WAITING,
                            pageable);
                } else {
                    bookings = bookingStorage.findBookingsByBookerIdAndStatus(userId, Status.WAITING, pageable);
                }
                break;
            case "REJECTED":
                if (isOwner) {
                    bookings = bookingStorage.findBookingsByItemOwnerIdAndStatus(userId, Status.REJECTED,
                            pageable);
                } else {
                    bookings = bookingStorage.findBookingsByBookerIdAndStatus(userId, Status.REJECTED, pageable);
                }
                break;
            default:
                if (isOwner) {
                    bookings = bookingStorage.findBookingsByItemOwnerId(userId, pageable);
                } else {
                    bookings = bookingStorage.findBookingsByBookerId(userId, pageable);
                }
        }
        return bookings.stream().map(BookingMapper::toExtendedBookingDto).collect(Collectors.toList());
    }
}
