package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.ItemMapper;
import ru.practicum.shareit.user.dto.UserMapper;

import java.util.Objects;

public class BookingMapper {

    public static BookingDto toBookingDto(Booking booking) {
        if (Objects.isNull(booking)) return null;
        return BookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemId(booking.getItem().getId())
                .bookerId(booking.getBooker().getId())
                .status(booking.getStatus())
                .build();
    }

    public static Booking toBooking(BookingDto bookingDto) {
        return Booking.builder()
                .id(bookingDto.getId())
                .start(bookingDto.getStart())
                .end(bookingDto.getEnd())
                .status(bookingDto.getStatus())
                .build();
    }

    public static SendingBookingDto toExtendedBookingDto(Booking booking) {
        return SendingBookingDto.builder()
                .id(booking.getId())
                .start(booking.getStart())
                .end(booking.getEnd())
                .itemDto(ItemMapper.toItemDto(booking.getItem(), null))
                .bookerDto(UserMapper.toUserDto(booking.getBooker()))
                .status(booking.getStatus())
                .build();
    }
}
