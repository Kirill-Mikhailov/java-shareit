package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.booking.BookingController;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class BookingControllerTest {
    @MockBean
    private BookingClient bookingClient;
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    private BookingDto bookingDto;

    private final ResponseEntity<Object> responseEntity = new ResponseEntity<>(HttpStatus.OK);

    @BeforeEach
    public void beforeEach() {
        this.bookingDto = BookingDto.builder()
                .id(1L)
                .bookerId(2L)
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .itemId(1L)
                .build();
    }

    @Test
    public void shouldAddBooking() throws Exception {
        when(bookingClient.addBooking(anyLong(), any(BookingDto.class)))
                .thenReturn(responseEntity);

        mvc.perform(post("/bookings")
                        .content(mapper.writeValueAsString(bookingDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
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
                .when(bookingClient.approveBooking(anyLong(), anyLong(), anyBoolean()))
                .thenReturn(responseEntity);

        mvc.perform(patch("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", String.valueOf(true))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldGetBookingById() throws Exception {
        Mockito
                .when(bookingClient.getBooking(anyLong(), anyLong()))
                .thenReturn(responseEntity);

        mvc.perform(get("/bookings/1")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldGetListOfUsersBookings() throws Exception {
        Mockito
                .when(bookingClient.getListOfUsersBookings(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(responseEntity);

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldGetListOfBookingsUserItems() throws Exception {
        Mockito
                .when(bookingClient.getListOfBookingsUserItems(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(responseEntity);

        mvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
