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
@Validated
@Slf4j
public class BookingController {

    private final BookingClient bookingClient;

    @PostMapping
    public ResponseEntity<Object> addBooking(@Validated @RequestBody BookingDto bookingDto,
                                             @Valid @Positive @RequestHeader(Util.HEADER_USER_ID) Long bookerId) {
        log.info("BookingController => addBooking: bookerId={}, bookingDto={}", bookerId, bookingDto);
        return bookingClient.addBooking(bookerId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> approveBooking(@Valid @Positive @RequestHeader(Util.HEADER_USER_ID) Long ownerId,
                                            @Valid @Positive @PathVariable Long bookingId,
                                            @Valid @NotNull @RequestParam("approved") Boolean approved) {
        log.info("BookingController => approveBooking: ownerId={}, bookingId={}, approved={}", ownerId, bookingId,
                approved);
        return bookingClient.approveBooking(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getBooking(@Valid @Positive @RequestHeader(Util.HEADER_USER_ID) Long userId,
                                        @Valid @Positive @PathVariable Long bookingId) {
        log.info("BookingController => getBooking: userId={}, bookingId={}", userId, bookingId);
        return bookingClient.getBooking(userId, bookingId);
    }

    @GetMapping
    public ResponseEntity<Object> getListOfUsersBookings(
            @Valid @Positive @RequestHeader(Util.HEADER_USER_ID) Long bookerId,
            @Valid @Pattern(regexp = "ALL|CURRENT|PAST|FUTURE|WAITING|REJECTED", message = "Unknown state: ")
            @RequestParam(value = "state", required = false, defaultValue = "ALL") String state,
            @Valid @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
            @Valid @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("BookingController => getListOfUsersBookings: bookerId={}, state={}, from={}, size={}", bookerId,
                state, from, size);
        return bookingClient.getListOfUsersBookings(bookerId, state, from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getListOfBookingsUserItems(
            @Valid @Positive @RequestHeader(Util.HEADER_USER_ID) Long ownerId,
            @Valid @Pattern(regexp = "ALL|CURRENT|PAST|FUTURE|WAITING|REJECTED", message = "Unknown state: ")
            @RequestParam(value = "state", required = false, defaultValue = "ALL") String state,
            @Valid @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
            @Valid @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        log.info("BookingController => getListOfBookingsUserItems: ownerId={}, state={}, from={}, size={}", ownerId,
                state, from, size);
        return bookingClient.getListOfBookingsUserItems(ownerId, state, from, size);
    }
}
