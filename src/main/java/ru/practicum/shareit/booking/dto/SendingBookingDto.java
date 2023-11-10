package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
@Builder
public class SendingBookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private ItemDto itemDto;
    private UserDto bookerDto;
    private Status status;

    @JsonProperty("item")
    public ItemDto getItemDto() {
        return itemDto;
    }

    @JsonProperty("booker")
    public UserDto getBookerDto() {
        return bookerDto;
    }
}
