package ru.practicum.shareit.booking.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.SendingBookingDto;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public SendingBookingDto addBooking(@Validated @RequestBody BookingDto bookingDto,
                                        @Valid @Positive @RequestHeader("X-Sharer-User-Id") Long bookerId) {
        bookingDto.setBookerId(bookerId);
        bookingDto.setStatus(Status.WAITING);
        return bookingService.addBooking(bookingDto);
    }

    @PatchMapping("/{bookingId}")
    public SendingBookingDto approveBooking(@Valid @Positive @RequestHeader("X-Sharer-User-Id") Long ownerId,
                                            @Valid @Positive @PathVariable Long bookingId,
                                            @Valid @NotNull @RequestParam("approved") Boolean approved) {
        return bookingService.approveBooking(ownerId, bookingId, approved);
    }

    @GetMapping("/{bookingId}")
    public SendingBookingDto getBooking(@Valid @Positive @RequestHeader("X-Sharer-User-Id") Long userId,
                                        @Valid @Positive @PathVariable Long bookingId) {
        return bookingService.getBooking(userId, bookingId);
    }

    @GetMapping
    public List<SendingBookingDto> getListOfUsersBookings(@Valid @Positive
                                                               @RequestHeader("X-Sharer-User-Id") Long bookerId,
                                                          @Valid
                                                          @Pattern(regexp = "ALL|CURRENT|PAST|FUTURE|WAITING|REJECTED",
                                                                  message = "Unknown state: ")
                                                          @RequestParam(value = "state", required = false,
                                                                   defaultValue = "ALL") String state) {
        return bookingService.getListOfUsersBookings(bookerId, state);
    }

    @GetMapping("/owner")
    public List<SendingBookingDto> getListOfBookingsUserItems(@Valid @Positive
                                                              @RequestHeader("X-Sharer-User-Id") Long ownerId,
                                                              @Valid
                                                              @Pattern(regexp =
                                                                      "ALL|CURRENT|PAST|FUTURE|WAITING|REJECTED",
                                                                      message = "Unknown state: ")
                                                              @RequestParam(value = "state", required = false,
                                                                       defaultValue = "ALL") String state) {
        return bookingService.getListOfBookingsUserItems(ownerId, state);
    }
}