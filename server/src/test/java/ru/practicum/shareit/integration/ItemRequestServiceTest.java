package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ItemRequestServiceTest {
    private final EntityManager em;
    private final ItemRequestService itemRequestService;
    private final UserService userService;

    @Test
    public void shouldGetItemRequestsByUserId() {
        ItemRequestDto itemRequestDto = ItemRequestDto.builder()
                .description("description")
                .build();

        UserDto userDto = UserDto.builder()
                .name("name")
                .email("user@email.com")
                .build();

        userService.add(userDto);
        TypedQuery<User> query = em.createQuery("select u from User u where u.email = :email", User.class);
        User user = query.setParameter("email", userDto.getEmail()).getSingleResult();


        itemRequestService.addItemRequest(itemRequestDto, user.getId());
        List<ItemRequestDto> itemRequests = itemRequestService.getItemRequests(user.getId());
        ItemRequestDto itemRequestDtoOutgoing = itemRequests.get(0);

        assertThat(1, equalTo(itemRequests.size()));
        assertThat(itemRequestDtoOutgoing.getId(), notNullValue());
        assertThat(itemRequestDtoOutgoing.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(itemRequestDtoOutgoing.getItems(), equalTo(Collections.emptyList()));
    }

    @Test
    public void shouldNotGetItemRequestsByUserIdWhenUserNotFound() {
        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> itemRequestService.getItemRequests(1L)
        );

        assertThat(e.getMessage(), equalTo("Пользователя с таким id не существует"));
    }
}
