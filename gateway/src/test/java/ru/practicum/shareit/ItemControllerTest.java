package ru.practicum.shareit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.item.ItemController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ItemControllerTest {
    @MockBean
    private ItemClient itemClient;
    private final ObjectMapper mapper;
    private final MockMvc mvc;

    private ItemDto itemDto;

    private final ResponseEntity<Object> responseEntity = new ResponseEntity<>(HttpStatus.OK);

    @BeforeEach
    public void beforeEach() {
        this.itemDto = ItemDto.builder()
                .id(1L)
                .name("item")
                .description("description")
                .available(true)
                .comments(Collections.emptyList())
                .build();
    }

    @Test
    public void shouldGetItemById() throws Exception {
        Mockito
                .when(itemClient.getItemById(anyLong(), anyLong()))
                .thenReturn(responseEntity);

        mvc.perform(get("/items/1")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldGetItems() throws Exception {
        Mockito
                .when(itemClient.getItems(anyLong(), anyInt(), anyInt()))
                .thenReturn(responseEntity);

        mvc.perform(get("/items")
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldAddItem() throws Exception {
        Mockito
                .when(itemClient.addItem(anyLong(), ArgumentMatchers.any(ItemDto.class)))
                .thenReturn(responseEntity);

        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotAddItemWhenNameIsBlank() throws Exception {
        itemDto.setName("");
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка с полем name: Название не может быть пустым"));
    }

    @Test
    public void shouldNotAddItemWhenIsAvailableNull() throws Exception {
        itemDto.setAvailable(null);
        mvc.perform(post("/items")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка с полем available: must not be null"));
    }

    @Test
    public void shouldUpdateItem() throws Exception {
        Mockito
                .when(itemClient.updateItem(anyLong(), anyLong(), ArgumentMatchers.any(ItemDto.class)))
                .thenReturn(responseEntity);

        mvc.perform(patch("/items/1")
                        .content(mapper.writeValueAsString(itemDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldSearchItems() throws Exception {
        Mockito
                .when(itemClient.searchItems(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(responseEntity);

        mvc.perform(get("/items/search")
                        .header("X-Sharer-User-Id", 1)
                        .param("text", "item")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldAddComment() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("text")
                .authorName("userName")
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();
        Mockito
                .when(itemClient.addComment(anyLong(), anyLong(), ArgumentMatchers.any(CommentDto.class)))
                .thenReturn(responseEntity);

        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotAddCommentWhenBlankText() throws Exception {
        CommentDto commentDto = CommentDto.builder()
                .id(1L)
                .text("")
                .authorName("userName")
                .created(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS))
                .build();

        mvc.perform(post("/items/1/comment")
                        .content(mapper.writeValueAsString(commentDto))
                        .header("X-Sharer-User-Id", 1)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Ошибка с полем text: Текст комментария не может быть пустым"));
    }
}
