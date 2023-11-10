package ru.practicum.shareit.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userStorage;

    @Override
    public List<UserDto> getAll() {
        return userStorage.findAll().stream().map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto add(UserDto userDto) {
        return UserMapper.toUserDto(userStorage.save(UserMapper.toUser(userDto)));
    }

    @Override
    public UserDto update(UserDto userDto) {
        User userFromDb = userStorage.findById(userDto.getId())
                .orElseThrow(() -> new UserNotFoundException("Пользователя с таким id не существует"));
        if (Objects.nonNull(userDto.getName())) userFromDb.setName(userDto.getName());
        if (Objects.nonNull(userDto.getEmail())) userFromDb.setEmail(userDto.getEmail());
        return UserMapper.toUserDto(userStorage.save(userFromDb));
    }

    @Override
    public UserDto getUserById(Long id) {
        return UserMapper.toUserDto(userStorage.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Пользователя с таким id не существует")));
    }

    @Override
    public void delete(Long id) {
        userStorage.deleteById(id);
    }
}
