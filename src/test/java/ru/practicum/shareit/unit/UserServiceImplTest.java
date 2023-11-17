package ru.practicum.shareit.unit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserServiceImpl;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userStorage;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    public void beforeEach() {
        this.user = User.builder()
                .id(1L)
                .name("name")
                .email("email")
                .build();
    }

    @Test
    void shouldGetAllUsersTest() {
        Mockito
                .when(userStorage.findAll())
                .thenReturn(List.of(user));

        List<UserDto> users = userService.getAll();

        assertThat(users.size(), equalTo(1));
        assertThat(users.get(0).getId(), equalTo(user.getId()));
        assertThat(users.get(0).getName(), equalTo(user.getName()));
        assertThat(users.get(0).getEmail(), equalTo(user.getEmail()));
    }

    @Test
    void shouldAddUserTest() {
        Mockito
                .when(userStorage.save(user))
                .thenReturn(user);

        UserDto userDto = userService.add(UserMapper.toUserDto(user));

        assertThat(user.getId(), equalTo(userDto.getId()));
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
        verify(userStorage, times(1)).save(user);
    }

    @Test
    void shouldUpdateUserTest() {
        User newUser = User.builder()
                .id(1L)
                .name("newName")
                .email("newEmail")
                .build();
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(userStorage.save(newUser))
                .thenReturn(newUser);

        UserDto updatedUserDto = userService.update(UserMapper.toUserDto(newUser));

        assertThat(newUser.getId(), equalTo(updatedUserDto.getId()));
        assertThat(newUser.getName(), equalTo(updatedUserDto.getName()));
        assertThat(newUser.getEmail(), equalTo(updatedUserDto.getEmail()));
        verify(userStorage, times(1)).save(user);
        verify(userStorage, times(1)).findById(anyLong());
    }

    @Test
    void shouldNotUpdateUserWhenUserNotFoundTest() {
        User newUser = User.builder()
                .id(1L)
                .name("newName")
                .email("newEmail")
                .build();
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> userService.update(UserMapper.toUserDto(newUser))
        );

        assertThat("Пользователя с таким id не существует", equalTo(e.getMessage()));
    }

    @Test
    void shouldUpdateUserNameTest() {
        User newUser = User.builder()
                .id(1L)
                .name("newName")
                .build();
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(userStorage.save(ArgumentMatchers.any(User.class)))
                .thenReturn(user);

        UserDto updatedUserDto = userService.update(UserMapper.toUserDto(newUser));

        assertThat(1L, equalTo(updatedUserDto.getId()));
        assertThat("newName", equalTo(updatedUserDto.getName()));
        assertThat("email", equalTo(updatedUserDto.getEmail()));
        verify(userStorage, times(1)).save(user);
    }

    @Test
    void shouldUpdateUserEmailTest() {
        User newUser = User.builder()
                .id(1L)
                .email("newEmail")
                .build();
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user));
        Mockito
                .when(userStorage.save(ArgumentMatchers.any(User.class)))
                .thenReturn(user);

        UserDto updatedUserDto = userService.update(UserMapper.toUserDto(newUser));

        assertThat(1L, equalTo(updatedUserDto.getId()));
        assertThat("name", equalTo(updatedUserDto.getName()));
        assertThat("newEmail", equalTo(updatedUserDto.getEmail()));
        verify(userStorage, times(1)).save(user);
    }

    @Test
    void ShouldGetUserByIdTest() {
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.of(user));

        UserDto userDto = userService.getUserById(1L);

        assertThat(user.getId(), equalTo(userDto.getId()));
        assertThat(user.getName(), equalTo(userDto.getName()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
        verify(userStorage, times(1)).findById(anyLong());
    }

    @Test
    void ShouldNotGetUserByIdWhenUserNotFoundTest() {
        Mockito
                .when(userStorage.findById(anyLong()))
                .thenReturn(Optional.empty());

        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(anyLong())
        );

        assertThat("Пользователя с таким id не существует", equalTo(e.getMessage()));
        verify(userStorage, times(1)).findById(anyLong());
    }

    @Test
    void delete() {
        userService.delete(1L);

        verify(userStorage, times(1)).deleteById(1L);
    }
}