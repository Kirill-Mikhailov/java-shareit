package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.markers.AddItemValidation;
import ru.practicum.shareit.item.dto.markers.UpdateItemValidation;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
public class ItemDto {
    private Long id;
    @NotBlank(groups = AddItemValidation.class, message = "Название не может быть пустым")
    private String name;
    @Size(groups = {AddItemValidation.class, UpdateItemValidation.class},
            max = 1000, message = "Максимальная длина описания — 1000 символов")
    @NotNull(groups = AddItemValidation.class)
    private String description;
    private Long ownerId;
    @NotNull(groups = AddItemValidation.class)
    private Boolean available;
    private Long requestId;
}
