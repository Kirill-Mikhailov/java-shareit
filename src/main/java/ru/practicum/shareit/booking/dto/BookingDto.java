package ru.practicum.shareit.booking.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.Status;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDateTime;

@Data
@Builder
public class BookingDto {
    private Long id;
    @NotNull
    @Future(message = "Дата начала бронирования не может быть в прошлом")
    private LocalDateTime start;
    @NotNull
    @Future(message = "Дата окончания бронирования не может быть в прошлом")
    private LocalDateTime end;
    @NotNull(message = "id бронируемой вещи не может быть пустым")
    @Positive(message = "id бронируемой вещи должно быть положительным")
    private Long itemId;
    private Long bookerId;
    private Status status;
}
