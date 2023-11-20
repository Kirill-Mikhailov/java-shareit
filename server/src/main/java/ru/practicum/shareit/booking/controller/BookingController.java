package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Util.Util;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.SendingBookingDto;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public SendingBookingDto addBooking(@RequestBody BookingDto bookingDto,
                                        @RequestHeader(Util.HEADER_USER_ID) Long bookerId) {
        log.info("BookingController => addBooking: bookerId={}, bookingDto={}", bookerId, bookingDto);
        bookingDto.setBookerId(bookerId);
        bookingDto.setStatus(Status.WAITING);
        return bookingService.addBooking(bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public SendingBookingDto approveBooking(@RequestHeader(Util.HEADER_USER_ID) Long ownerId,
                                            @PathVariable Long bookingId,
                                            @RequestParam("approved") Boolean approved) {
        log.info("BookingController => approveBooking: ownerId={}, bookingId={}, approved={}", ownerId, bookingId,
                approved);
        return bookingService.approveBooking(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public SendingBookingDto getBooking(@RequestHeader(Util.HEADER_USER_ID) Long userId,
                                        @PathVariable Long bookingId) {
        log.info("BookingController => getBooking: userId={}, bookingId={}", userId, bookingId);
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<SendingBookingDto> getListOfUsersBookings(
            @RequestHeader(Util.HEADER_USER_ID) Long bookerId,
            @RequestParam(value = "state", required = false, defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") int from,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("BookingController => getListOfUsersBookings: bookerId={}, state={}, from={}, size={}", bookerId,
                state, from, size);
        return bookingService.getListOfBookingsUserItemsOrUserBookings(bookerId, state, from, size, false);
    }

    @GetMapping("/owner")
    public List<SendingBookingDto> getListOfBookingsUserItems(
            @RequestHeader(Util.HEADER_USER_ID) Long ownerId,
            @RequestParam(value = "state", required = false, defaultValue = "ALL") String state,
            @RequestParam(value = "from", defaultValue = "0") int from,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("BookingController => getListOfBookingsUserItems: ownerId={}, state={}, from={}, size={}", ownerId,
                state, from, size);
        return bookingService.getListOfBookingsUserItemsOrUserBookings(ownerId, state, from, size, true);
    }
}
