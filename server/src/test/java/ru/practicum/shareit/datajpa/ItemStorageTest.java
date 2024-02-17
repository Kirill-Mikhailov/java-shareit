package ru.practicum.shareit.datajpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemStorageTest {
    private final ItemRepository itemStorage;
    private final UserRepository userStorage;

    @Test
    public void shouldFindByText() {
        User user = User.builder()
                .name("name")
                .email("user@email.com")
                .build();


        Item item = Item.builder()
                .name("electric drill")
                .description("electric drill with drills and charging in a suitcase")
                .available(true)
                .owner(user)
                .build();

        userStorage.save(user);
        itemStorage.save(item);

        List<Item> items = itemStorage.searchItems("drill", PageRequest.of(0, 5)).toList();

        assertThat(items.size(), equalTo(1));
        assertThat(items.get(0), equalTo(item));

        items = itemStorage.searchItems("screwdriver", PageRequest.of(0, 5)).toList();

        assertThat(items.size(), equalTo(0));
    }
}
