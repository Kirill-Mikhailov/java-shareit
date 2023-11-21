package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.markers.AddUserValidation;
import ru.practicum.shareit.user.dto.markers.UpdateUserValidation;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> getUsers() {
        log.info("UserController => getUsers");
        return userClient.getUsers();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<Object>  getUserById(@Valid @Positive @PathVariable Long userId) {
        log.info("UserController => getUserById: userId={}", userId);
        return userClient.getUserById(userId);
    }

    @PostMapping
    public ResponseEntity<Object>  addUser(@Validated(AddUserValidation.class) @RequestBody UserDto userDto) {
        log.info("UserController => addUser: userDto={}", userDto);
        return userClient.addUser(userDto);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<Object>  updateUser(@Validated(UpdateUserValidation.class) @RequestBody UserDto userDto,
                              @Valid @Positive @PathVariable Long userId) {
        log.info("UserController => updateUser: userId={}, userDto={}", userId, userDto);
        return userClient.updateUser(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Object> deleteUser(@Valid @Positive @PathVariable Long userId) {
        log.info("UserController => deleteUser: userId={}", userId);
        return userClient.deleteUser(userId);
    }
}
