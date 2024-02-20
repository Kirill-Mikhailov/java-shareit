package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.util.Util;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(@Valid @RequestBody BookingDto bookingDto,
                                             @Positive @RequestHeader(Util.HEADER_USER_ID) Long bookerId) {
        log.info("BookingController => addBooking: bookerId={}, bookingDto={}", bookerId, bookingDto);
        return bookingClient.addBooking(bookerId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@Positive @RequestHeader(Util.HEADER_USER_ID) Long ownerId,
                                            @Positive @PathVariable Long bookingId,
                                            @NotNull @RequestParam("approved") Boolean approved) {
        log.info("BookingController => approveBooking: ownerId={}, bookingId={}, approved={}", ownerId, bookingId,
                approved);
        return bookingClient.approveBooking(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@Positive @RequestHeader(Util.HEADER_USER_ID) Long userId,
                                        @Positive @PathVariable Long bookingId) {
        log.info("BookingController => getBooking: userId={}, bookingId={}", userId, bookingId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getListOfUsersBookings(
            @Positive @RequestHeader(Util.HEADER_USER_ID) Long bookerId,
            @Pattern(regexp = "ALL|CURRENT|PAST|FUTURE|WAITING|REJECTED", message = "Unknown state: ")
            @RequestParam(value = "state", required = false, defaultValue = "ALL") String state,
            @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
            @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("BookingController => getListOfUsersBookings: bookerId={}, state={}, from={}, size={}", bookerId,
                state, from, size);
        return bookingClient.getListOfUsersBookings(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getListOfBookingsUserItems(
            @Positive @RequestHeader(Util.HEADER_USER_ID) Long ownerId,
            @Pattern(regexp = "ALL|CURRENT|PAST|FUTURE|WAITING|REJECTED", message = "Unknown state: ")
            @RequestParam(value = "state", required = false, defaultValue = "ALL") String state,
            @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
            @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("BookingController => getListOfBookingsUserItems: ownerId={}, state={}, from={}, size={}", ownerId,
                state, from, size);
        return bookingClient.getListOfBookingsUserItems(ownerId, state, from, size);
    }
}
