package ru.practicum.shareit.user.storage;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.exception.EmailAlreadyExistException;
import ru.practicum.shareit.exception.UserNotFoundException;

import java.util.*;

@Repository
public class UserStorageImpl implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> emails = new HashSet<>();
    private long id = 1;

    private Long getNextId() {
        return id++;
    }

    @Override
    public List<User> getAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User add(User user) {
        if (!emails.add(user.getEmail())) {
            throw new EmailAlreadyExistException("Пользователь с таким email уже существует");
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        if (!users.containsKey(user.getId())) {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }
        if (user.getEmail().equals(users.get(user.getId()).getEmail())) {
            users.put(user.getId(), user);
        } else {
            if (!emails.add(user.getEmail())) {
                throw new EmailAlreadyExistException("Пользователь с таким email уже существует");
            }
            emails.remove(users.get(user.getId()).getEmail());
            users.put(user.getId(), user);
        }
        return user;
    }

    @Override
    public User getUserById(Long id) {
        if (!users.containsKey(id)) {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }
        return users.get(id);
    }

    @Override
    public void delete(Long id) {
        if (!users.containsKey(id)) {
            throw new UserNotFoundException("Пользователя с таким id не существует");
        }
        emails.remove(users.get(id).getEmail());
        users.remove(id);
    }
}
