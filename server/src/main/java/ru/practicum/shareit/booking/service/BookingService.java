package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.SendingBookingDto;

import java.util.List;

public interface BookingService {
    SendingBookingDto addBooking(BookingDto bookingDto);

    SendingBookingDto approveBooking(Long ownerId, Long bookingId, Boolean approved);

    SendingBookingDto getBooking(Long userId, Long bookingId);

    List<SendingBookingDto> getListOfBookingsUserItemsOrUserBookings(Long ownerId, String state, int from, int size,
                                                                     boolean isOwner);
}
