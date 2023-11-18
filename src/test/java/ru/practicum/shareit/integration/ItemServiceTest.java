package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ItemServiceTest {
    private final EntityManager em;
    private final ItemService itemService;
    private final UserService userService;

    @Test
    public void shouldGetItemsByUserId() {
        ItemDto itemDto = ItemDto.builder()
                .name("name")
                .description("description")
                .available(true)
                .build();

        UserDto userDto = UserDto.builder()
                .name("name")
                .email("user@email.com")
                .build();

        userService.add(userDto);

        TypedQuery<User> query = em.createQuery("select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail()).getSingleResult();

        itemDto.setOwnerId(user.getId());
        itemService.addItem(itemDto);

        List<ItemDto> items = itemService.getItems(user.getId(), 0, 5);
        ItemDto itemDtoOutgoing = items.get(0);

        assertThat(items.size(), equalTo(1));
        assertThat(itemDtoOutgoing.getId(), notNullValue());
        assertThat(itemDtoOutgoing.getName(), equalTo(itemDto.getName()));
        assertThat(itemDtoOutgoing.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(itemDtoOutgoing.getAvailable(), equalTo(itemDto.getAvailable()));
        assertThat(itemDtoOutgoing.getRequestId(), nullValue());
        assertThat(itemDtoOutgoing.getLastBooking(), nullValue());
        assertThat(itemDtoOutgoing.getNextBooking(), nullValue());
        assertThat(itemDtoOutgoing.getComments(), equalTo(Collections.emptyList()));
    }

    @Test
    public void shouldNotGetItemsByUserIdWhenUserNotFound() {
        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> itemService.getItems(1L, 0, 5)
        );

        assertThat(e.getMessage(), equalTo("Пользователя с таким id не существует"));
    }
}

