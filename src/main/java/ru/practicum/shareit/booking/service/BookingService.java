package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.SendingBookingDto;

import java.util.List;

public interface BookingService {
    SendingBookingDto addBooking(BookingDto bookingDto);

    SendingBookingDto approveBooking(Long ownerId, Long bookingId, Boolean approved);

    SendingBookingDto getBooking(Long userId, Long bookingId);

    List<SendingBookingDto> getListOfUsersBookings(Long bookerId, String state);

    List<SendingBookingDto> getListOfBookingsUserItems(Long ownerId, String state);
}
