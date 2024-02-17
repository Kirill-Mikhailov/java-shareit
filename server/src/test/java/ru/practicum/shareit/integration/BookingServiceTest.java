package ru.practicum.shareit.integration;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.SendingBookingDto;
import ru.practicum.shareit.booking.model.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.exception.UserNotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Transactional
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class BookingServiceTest {
    private final EntityManager em;
    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;

    @Test
    public void shouldGetUserBookings() {
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

        TypedQuery<User> queryUser = em.createQuery("select u from User u where u.email = :email", User.class);
        User user = queryUser.setParameter("email", userDto.getEmail()).getSingleResult();

        itemDto.setOwnerId(user.getId());
        itemService.addItem(itemDto);

        userDto = UserDto.builder()
                .name("name2")
                .email("user2@email.com")
                .build();

        userService.add(userDto);

        queryUser = em.createQuery("select u from User u where u.email = :email", User.class);
        User user2 = queryUser.setParameter("email", userDto.getEmail()).getSingleResult();

        TypedQuery<Item> queryItem = em.createQuery("select i from Item i where i.name = :name", Item.class);
        Item item = queryItem.setParameter("name", itemDto.getName()).getSingleResult();

        BookingDto bookingDto = BookingDto.builder()
                .itemId(item.getId())
                .bookerId(user2.getId())
                .start(LocalDateTime.now().plusDays(1))
                .end(LocalDateTime.now().plusDays(2))
                .status(Status.WAITING)
                .build();

        bookingService.addBooking(bookingDto);

        List<SendingBookingDto> bookings = bookingService.getListOfBookingsUserItemsOrUserBookings(user2.getId(),
                "ALL", 0, 5, false);
        SendingBookingDto bookingDtoOutgoing = bookings.get(0);

        assertThat(bookings.size(), equalTo(1));
        assertThat(bookingDtoOutgoing.getId(), notNullValue());
        assertThat(bookingDtoOutgoing.getStart(), equalTo(bookingDto.getStart()));
        assertThat(bookingDtoOutgoing.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(bookingDtoOutgoing.getItem().getId(), equalTo(bookingDto.getItemId()));
        assertThat(bookingDtoOutgoing.getBooker().getId(), equalTo(bookingDto.getBookerId()));
        assertThat(bookingDtoOutgoing.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void shouldNotGetUserBookingsWhenUserNotFound() {
        UserNotFoundException e = assertThrows(
                UserNotFoundException.class,
                () -> bookingService.getListOfBookingsUserItemsOrUserBookings(1L, "ALL", 0, 5, false)
        );

        assertThat(e.getMessage(), equalTo("Пользователя с таким id не существует"));
    }
}
