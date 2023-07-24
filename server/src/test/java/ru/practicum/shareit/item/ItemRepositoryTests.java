package ru.practicum.shareit.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ItemRepositoryTests {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Test
    void testFindAllByOwnerId() {
        User user = userRepository.save(User.builder().name("name").email("email@email.com").build());
        itemRepository.save(Item.builder().name("name").description("description").available(true).owner(user).build());
        List<Item> items = itemRepository.findAllByOwnerId(user.getId());
        assertThat(items.stream().count(), equalTo(1L));
    }

    @Test
    void testFindAllByRequestId() {
        User user = userRepository.save(User.builder().name("name").email("email@email.com").build());
        User user2 = userRepository.save(User.builder().name("name2").email("email2@email.com").build());
        ItemRequest itemRequest = itemRequestRepository.save(ItemRequest.builder().description("item request descr")
                .requestorId(user2.getId()).created(LocalDateTime.now()).build());
        itemRepository.save(Item.builder().name("name").description("description").available(true)
                .owner(user).requestId(itemRequest.getId()).build());
        assertThat(itemRepository.findAllByOwnerId(user.getId()).size(), equalTo(1));
    }

}