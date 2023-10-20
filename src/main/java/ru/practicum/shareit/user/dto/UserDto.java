package ru.practicum.shareit.user.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.dto.markers.AddUserValidation;
import ru.practicum.shareit.user.dto.markers.UpdateUserValidation;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
public class UserDto {
    private Long id;
    @NotBlank(groups = AddUserValidation.class, message = "Имя не может быть пустым")
    private String name;
    @NotNull(groups = AddUserValidation.class)
    @Email(groups = {AddUserValidation.class, UpdateUserValidation.class}, message = "Некорректный email")
    private String email;
}
