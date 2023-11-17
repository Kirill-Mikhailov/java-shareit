package ru.practicum.shareit.webmvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.controller.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.SendingBookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingControllerTest {
    @MockBean
    private BookingService bookingService;
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    private BookingDto bookingDto;

    private SendingBookingDto sendingBookingDto;
    @BeforeEach
    public void beforeEach() {
        this.bookingDto = BookingDto.builder()
                .id(1L)
                .bookerId(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .status(Status.WAITING)
                .build();

        this.sendingBookingDto = SendingBookingDto.builder()
                .id(1L)
                .booker(
                        UserDto.builder()
                                .id(1L)
                                .name("name")
                                .email("email")
                                .build()
                )
                .item(
                        ItemDto.builder()
                                .id(1L)
                                .name("name")
                                .description("description")
                                .available(true)
                                .ownerId(2L)
                                .build()
                )
                .start(LocalDateTime.now().plusDays(1).truncatedTo(ChronoUnit.SECONDS))
                .end(LocalDateTime.now().plusDays(2).truncatedTo(ChronoUnit.SECONDS))
                .status(Status.WAITING)
                .build();
    }

    @Test
    public void shouldAddBooking() throws Exception {
        when(bookingService.addBooking(any(BookingDto.class)))
                .thenReturn(sendingBookingDto);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sendingBookingDto.getId()))
                .andExpect(jsonPath("$.start").value(sendingBookingDto.getStart().toString()))
                .andExpect(jsonPath("$.end").value(sendingBookingDto.getEnd().toString()))
                .andExpect(jsonPath("$.status").value(sendingBookingDto.getStatus().toString()))
                .andExpect(jsonPath("$.item.id").value(sendingBookingDto.getItem().getId()))
                .andExpect(jsonPath("$.booker.id").value(sendingBookingDto.getBooker().getId()));
    }

    @Test
    public void shouldNotAddBookingWhenStartNull() throws Exception {
        bookingDto.setStart(null);
        mvc.perform(post("/bookings")
                .content(mapper.writeValueAsString(bookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка с полем start: must not be null"));
    }

    @Test
    public void shouldNotAddBookingWhenStartBeforeNow() throws Exception {
        bookingDto.setStart(LocalDateTime.now().minusDays(1));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Ошибка с полем start: Дата начала бронирования не может быть в прошлом"));
    }

    @Test
    public void shouldNotAddBookingWhenEndNull() throws Exception {
        bookingDto.setEnd(null);
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка с полем end: must not be null"));
    }

    @Test
    public void shouldNotAddBookingWhenEndBeforeNow() throws Exception {
        bookingDto.setEnd(LocalDateTime.now().minusDays(1));
        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("Ошибка с полем end: Дата окончания бронирования не может быть в прошлом"));
    }

    @Test
    public void shouldApproveBooking() throws Exception {
        Mockito
                .when(bookingService.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(sendingBookingDto);

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", String.valueOf(true))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sendingBookingDto.getId()))
                .andExpect(jsonPath("$.start").value(sendingBookingDto.getStart().toString()))
                .andExpect(jsonPath("$.end").value(sendingBookingDto.getEnd().toString()))
                .andExpect(jsonPath("$.status").value(sendingBookingDto.getStatus().toString()))
                .andExpect(jsonPath("$.item.id").value(sendingBookingDto.getItem().getId()))
                .andExpect(jsonPath("$.booker.id").value(sendingBookingDto.getBooker().getId()));
    }

    @Test
    public void shouldGetBookingById() throws Exception {
        Mockito
                .when(bookingService.getBooking(anyLong(), anyLong()))
                .thenReturn(sendingBookingDto);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sendingBookingDto.getId()))
                .andExpect(jsonPath("$.start").value(sendingBookingDto.getStart().toString()))
                .andExpect(jsonPath("$.end").value(sendingBookingDto.getEnd().toString()))
                .andExpect(jsonPath("$.status").value(sendingBookingDto.getStatus().toString()))
                .andExpect(jsonPath("$.item.id").value(sendingBookingDto.getItem().getId()))
                .andExpect(jsonPath("$.booker.id").value(sendingBookingDto.getBooker().getId()));
    }

    @Test
    public void shouldGetListOfUsersBookings() throws Exception {
        Mockito
                .when(bookingService.getListOfBookingsUserItemsOrUserBookings(anyLong(), anyString(), anyInt(),
                        anyInt(), anyBoolean()))
                .thenReturn(List.of(sendingBookingDto));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].id").value(sendingBookingDto.getId()))
                .andExpect(jsonPath("$.[0].start").value(sendingBookingDto.getStart().toString()))
                .andExpect(jsonPath("$.[0].end").value(sendingBookingDto.getEnd().toString()))
                .andExpect(jsonPath("$.[0].status").value(sendingBookingDto.getStatus().toString()))
                .andExpect(jsonPath("$.[0].item.id").value(sendingBookingDto.getItem().getId()))
                .andExpect(jsonPath("$.[0].booker.id").value(sendingBookingDto.getBooker().getId()));
    }

    @Test
    public void shouldGetListOfBookingsUserItems() throws Exception {
        Mockito
                .when(bookingService.getListOfBookingsUserItemsOrUserBookings(anyLong(), anyString(), anyInt(),
                        anyInt(), anyBoolean()))
                .thenReturn(List.of(sendingBookingDto));

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$.[0].id").value(sendingBookingDto.getId()))
                .andExpect(jsonPath("$.[0].start").value(sendingBookingDto.getStart().toString()))
                .andExpect(jsonPath("$.[0].end").value(sendingBookingDto.getEnd().toString()))
                .andExpect(jsonPath("$.[0].status").value(sendingBookingDto.getStatus().toString()))
                .andExpect(jsonPath("$.[0].item.id").value(sendingBookingDto.getItem().getId()))
                .andExpect(jsonPath("$.[0].booker.id").value(sendingBookingDto.getBooker().getId()));
    }
}
