package com.example.bankcards.controller;

import com.example.bankcards.dto.CardCreateEditDto;
import com.example.bankcards.dto.CardReadDto;
import com.example.bankcards.dto.UserReadDto;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.enumpack.Role;
import com.example.bankcards.enumpack.Status;
import com.example.bankcards.filter.CardFilter;
import com.example.bankcards.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
@ExtendWith(MockitoExtension.class)
public class CardRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CardService cardService;

    @InjectMocks
    private CardRestController cardRestController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(cardRestController).build();
    }

    @Test
    void findAll() throws Exception {
        CardFilter filter = CardFilter.builder().build();
        PageRequest pageable = PageRequest.of(0, 20);
        Page<CardReadDto> pageCardReadDto = getPageCardReadDto();
        doReturn(pageCardReadDto).when(cardService).findAll(filter, pageable);

        mockMvc.perform(get("/api/v1/cards")
                        .queryParam("page", String.valueOf(0))
                        .queryParam("size", String.valueOf(20)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(cardService).findAll(filter, pageable);
    }

    @Test
    void findAllByUserId() throws Exception {
        CardFilter filter = CardFilter.builder().build();
        PageRequest pageable = PageRequest.of(0, 20);
        Page<CardReadDto> pageCardReadDto = getPageCardReadDto();
        UserReadDto userReadDto = getUserReadDto();
        doReturn(pageCardReadDto).when(cardService).findAllByUserId(userReadDto.getId(), pageable, filter);

        mockMvc.perform(get("/api/v1/users/" + userReadDto.getId() + "/cards")
                        .queryParam("page", String.valueOf(0))
                        .queryParam("size", String.valueOf(20))
                        .queryParam("id", String.valueOf(userReadDto.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(userReadDto.getId())))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(cardService).findAllByUserId(userReadDto.getId(), pageable, filter);
    }

    @Test
    void findByIdSuccess() throws Exception {
        CardReadDto cardReadDto = getCardReadDto();
        doReturn(cardReadDto).when(cardService).findById(cardReadDto.getId());

        mockMvc.perform(get("/api/v1/cards/" + cardReadDto.getId())
                        .queryParam("id", String.valueOf(cardReadDto.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(cardReadDto.getId())))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(cardService).findById(cardReadDto.getId());
    }

    @Test
    void getBalanceSuccess() throws Exception {
        CardReadDto cardReadDto = getCardReadDto();
        doReturn(cardReadDto.getBalance()).when(cardService).getBalance(cardReadDto.getId());

        mockMvc.perform(get("/api/v1/cards/" + cardReadDto.getId() + "/balance")
                        .queryParam("id", String.valueOf(cardReadDto.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(cardReadDto.getId())))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(cardService).getBalance(cardReadDto.getId());
    }

    @Test
    void createSuccess() throws Exception {
        CardCreateEditDto cardCreateEditDto = getCardCreateEditDto();
        CardReadDto cardReadDto = getCardReadDto();
        doReturn(cardReadDto).when(cardService).create(cardCreateEditDto);

        mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .writeValueAsBytes(cardCreateEditDto)))
                .andExpect(status().isCreated());

        verify(cardService).create(cardCreateEditDto);
    }

    @Test
    void createFailedIfNoValidValue() throws Exception {
        CardCreateEditDto cardCreateEditDto = CardCreateEditDto.builder().build();

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsBytes(cardCreateEditDto)))
                .andExpect(status().is4xxClientError())
                .andReturn();
        Exception actualException = mvcResult.getResolvedException();

        assertNotNull(actualException);
        assertInstanceOf(MethodArgumentNotValidException.class, actualException);
        verifyNoInteractions(cardService);
    }

    @Test
    void updateSuccess() throws Exception {
        CardCreateEditDto cardCreateEditDto = getCardCreateEditDto();
        CardReadDto cardReadDto = getCardReadDto();
        doReturn(cardReadDto).when(cardService).update(cardReadDto.getId(), cardCreateEditDto);

        mockMvc.perform(put("/api/v1/cards/" + cardReadDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .writeValueAsBytes(cardCreateEditDto)))
                .andExpect(status().isOk());

        verify(cardService).update(cardReadDto.getId(), cardCreateEditDto);
    }

    @Test
    void updateFailedIfNoValidValue() throws Exception {
        CardCreateEditDto cardCreateEditDto = CardCreateEditDto.builder().build();
        Card card = getCard();

        MvcResult mvcResult = mockMvc.perform(put("/api/v1/cards/" + card.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsBytes(cardCreateEditDto)))
                .andExpect(status().is4xxClientError())
                .andReturn();
        Exception actualException = mvcResult.getResolvedException();

        assertNotNull(actualException);
        assertInstanceOf(MethodArgumentNotValidException.class, actualException);
        verifyNoInteractions(cardService);
    }

    @Test
    void blockingCardSuccess() throws Exception {
        Card card = getCard();
        CardReadDto cardReadDto = CardReadDto.builder().status(Status.BLOCKED).build();
        doReturn(cardReadDto).when(cardService).blockingCard(card.getId());

        mockMvc.perform(put("/api/v1/cards/" + card.getId() + "/blocked")
                        .queryParam("id", String.valueOf(card.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(card.getId())))
                .andExpect(status().isOk());

        assertEquals(cardReadDto.getStatus(), Status.BLOCKED);
        verify(cardService).blockingCard(card.getId());
    }

    @Test
    void deleteSuccess() throws Exception {
        Card card = getCard();
        doReturn(true).when(cardService).delete(card.getId());
        mockMvc.perform(delete("/api/v1/cards/" + card.getId()))
                .andExpect(status().isNoContent());

        verify(cardService).delete(card.getId());
    }

    @Test
    void deleteFailedIfCardNotFound() throws Exception {
        Card card = getCard();
        doReturn(false).when(cardService).delete(card.getId());

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/cards/" + card.getId()))
                .andExpect(status().isNotFound())
                .andReturn();
        Exception actualException = mvcResult.getResolvedException();

        assertNotNull(actualException);
        assertInstanceOf(ResponseStatusException.class, actualException);
        verify(cardService).delete(card.getId());
    }

    private static User getUser() {
        return User.builder()
                .id(1L)
                .firstname("Ivan")
                .lastname("Ivanov")
                .phoneNumber("11-11-111")
                .email("ivan@gmail.com")
                .role(Role.USER)
                .birthDate(LocalDate.of(2000, 11, 11))
                .passportNumber("HB111111")
                .build();
    }

    private static Card getCard() {
        User user = getUser();
        return Card.builder()
                .id(1L)
                .number("1234123412341234")
                .user(user)
                .expirationDate(LocalDate.of(2030, 11, 11))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(100.00))
                .build();
    }

    private static UserReadDto getUserReadDto() {
        return UserReadDto.builder()
                .id(1L)
                .firstname("Ivan")
                .lastname("Ivanov")
                .phoneNumber("11-11-111")
                .email("ivan@gmail.com")
                .role(Role.USER)
                .birthDate(LocalDate.of(2000, 11, 11))
                .passportNumber("HB1111111")
                .build();
    }

    private static CardReadDto getCardReadDto() {
        UserReadDto userReadDto = getUserReadDto();
        return CardReadDto.builder()
                .id(1L)
                .number("1234123412341234")
                .userReadDto(userReadDto)
                .expirationDate(LocalDate.of(2030, 11, 11))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(100.00))
                .build();
    }

    private static CardCreateEditDto getCardCreateEditDto() {
        User user = getUser();
        return CardCreateEditDto.builder()
                .number("1234123412341234")
                .userId(user.getId())
                .expirationDate(LocalDate.of(2030, 11, 11))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(100.00))
                .build();
    }

    private static Page<CardReadDto> getPageCardReadDto() {
        List<CardReadDto> cardList = new ArrayList<>();
        UserReadDto userReadDto = getUserReadDto();
        CardReadDto card1 = CardReadDto.builder()
                .id(1L)
                .number("1234123412341234")
                .userReadDto(userReadDto)
                .expirationDate(LocalDate.of(2030, 11, 11))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(100.00))
                .build();
        CardReadDto card2 = CardReadDto.builder()
                .id(2L)
                .number("4321432143214321")
                .userReadDto(userReadDto)
                .expirationDate(LocalDate.of(2035, 12, 12))
                .status(Status.ACTIVE)
                .balance(BigDecimal.valueOf(100.00))
                .build();
        cardList.add(card1);
        cardList.add(card2);
        return new PageImpl<>(cardList);
    }
}

