package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.User;
import com.example.bankcards.enumpack.Role;
import com.example.bankcards.filter.TransferFilter;
import com.example.bankcards.service.TransferService;
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
public class TransferRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransferService transferService;

    @InjectMocks
    private TransferRestController transferRestController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(transferRestController).build();
    }

    @Test
    void findAll() throws Exception {
        TransferFilter filter = TransferFilter.builder().build();
        PageRequest pageable = PageRequest.of(0, 20);
        Page<TransferReadDto> pageTransferReadDto = getPageTransferReadDto();
        doReturn(pageTransferReadDto).when(transferService).findAll(filter, pageable);

        mockMvc.perform(get("/api/v1/transfers")
                        .queryParam("page", String.valueOf(0))
                        .queryParam("size", String.valueOf(20)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(transferService).findAll(filter, pageable);
    }

    @Test
    void findAllByUserId() throws Exception {
        PageRequest pageable = PageRequest.of(0, 20);
        Page<TransferReadDto> pageTransferReadDto = getPageTransferReadDto();
        User user = getUser();
        doReturn(pageTransferReadDto).when(transferService).findAllByUserId(user.getId(), pageable);

        mockMvc.perform(get("/api/v1/users/" + user.getId() + "/transfers")
                        .queryParam("page", String.valueOf(0))
                        .queryParam("size", String.valueOf(20))
                        .queryParam("id", String.valueOf(user.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(user.getId())))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(transferService).findAllByUserId(user.getId(), pageable);
    }

    @Test
    void findByIdSuccess() throws Exception {
        TransferReadDto transferReadDto = getTransferReadDto();
        doReturn(transferReadDto).when(transferService).findById(transferReadDto.getId());

        mockMvc.perform(get("/api/v1/transfers/" + transferReadDto.getId())
                        .queryParam("id", String.valueOf(transferReadDto.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(transferReadDto.getId())))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(transferService).findById(transferReadDto.getId());
    }

    @Test
    void createSuccess() throws Exception {
        User user = getUser();
        TransferCreateEditDto transferCreateEditDto = getTransferCreateEditDto();
        TransferReadDto transferReadDto = getTransferReadDto();
        doReturn(transferReadDto).when(transferService).create(transferCreateEditDto);

        mockMvc.perform(post("/api/v1/users/" + user.getId() +"/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .writeValueAsBytes(transferCreateEditDto)))
                .andExpect(status().isCreated());

        verify(transferService).create(transferCreateEditDto);
    }

    @Test
    void createFailedIfNoValidValue() throws Exception {
        User user = getUser();
        CardCreateEditDto cardCreateEditDto = CardCreateEditDto.builder().build();

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/users/" + user.getId() +"/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsBytes(cardCreateEditDto)))
                .andExpect(status().is4xxClientError())
                .andReturn();
        Exception actualException = mvcResult.getResolvedException();

        assertNotNull(actualException);
        assertInstanceOf(MethodArgumentNotValidException.class, actualException);
        verifyNoInteractions(transferService);
    }

    @Test
    void updateSuccess() throws Exception {
        TransferCreateEditDto transferCreateEditDto = getTransferCreateEditDto();
        TransferReadDto transferReadDto = getTransferReadDto();
        doReturn(transferReadDto).when(transferService).update(transferReadDto.getId(), transferCreateEditDto);

        mockMvc.perform(put("/api/v1/transfers/" + transferReadDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .writeValueAsBytes(transferCreateEditDto)))
                .andExpect(status().isOk());

        verify(transferService).update(transferReadDto.getId(), transferCreateEditDto);
    }

    @Test
    void updateFailedIfNoValidValue() throws Exception {
        TransferCreateEditDto transferCreateEditDto = TransferCreateEditDto.builder().build();
        Transfer transfer = getTransfer();

        MvcResult mvcResult = mockMvc.perform(put("/api/v1/transfers/" + transfer.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .writeValueAsBytes(transferCreateEditDto)))
                .andExpect(status().is4xxClientError())
                .andReturn();
        Exception actualException = mvcResult.getResolvedException();

        assertNotNull(actualException);
        assertInstanceOf(MethodArgumentNotValidException.class, actualException);
        verifyNoInteractions(transferService);
    }

    @Test
    void deleteSuccess() throws Exception {
        Transfer transfer = getTransfer();
        doReturn(true).when(transferService).delete(transfer.getId());

        mockMvc.perform(delete("/api/v1/transfers/" + transfer.getId()))
                .andExpect(status().isNoContent());

        verify(transferService).delete(transfer.getId());
    }

    @Test
    void deleteFailed() throws Exception {
        Transfer transfer = getTransfer();
        doReturn(false).when(transferService).delete(transfer.getId());

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/transfers/" + transfer.getId()))
                .andExpect(status().is4xxClientError())
                .andReturn();
        Exception actualException = mvcResult.getResolvedException();

        assertNotNull(actualException);
        assertInstanceOf(ResponseStatusException.class, actualException);
        verify(transferService).delete(transfer.getId());
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

    private static Transfer getTransfer() {
        User user = getUser();
        return Transfer.builder()
                .id(1L)
                .user(user)
                .cardFrom("1234123412341234")
                .cardTo("4321432143214321")
                .transferDate(LocalDate.now())
                .sum(BigDecimal.valueOf(50.00))
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

    private static TransferReadDto getTransferReadDto() {
        UserReadDto userReadDto = getUserReadDto();
        return TransferReadDto.builder()
                .id(1L)
                .userReadDto(userReadDto)
                .cardFrom("1234123412341234")
                .cardTo("4321432143214321")
                .transferDate(LocalDate.now())
                .sum(BigDecimal.valueOf(50.00))
                .build();
    }

    private static TransferCreateEditDto getTransferCreateEditDto() {
        User user = getUser();
        return TransferCreateEditDto.builder()
                .userId(user.getId())
                .cardFrom("1234123412341234")
                .cardTo("4321432143214321")
                .sum(BigDecimal.valueOf(100.00))
                .build();
    }

    private static Page<TransferReadDto> getPageTransferReadDto() {
        List<TransferReadDto> transferList = new ArrayList<>();
        UserReadDto userReadDto = getUserReadDto();
        TransferReadDto transfer1 = TransferReadDto.builder()
                .id(1L)
                .userReadDto(userReadDto)
                .cardFrom("1234123412341234")
                .cardTo("4321432143214321")
                .transferDate(LocalDate.now())
                .sum(BigDecimal.valueOf(50.00))
                .build();
        TransferReadDto transfer2 = TransferReadDto.builder()
                .id(2L)
                .userReadDto(userReadDto)
                .cardFrom("1234123412341234")
                .cardTo("4321432143214321")
                .transferDate(LocalDate.now())
                .sum(BigDecimal.valueOf(50.00))
                .build();
        transferList.add(transfer1);
        transferList.add(transfer2);
        return new PageImpl<>(transferList);
    }
}

