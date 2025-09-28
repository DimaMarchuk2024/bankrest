package com.example.bankcards.controller;

import com.example.bankcards.dto.UserCreateEditDto;
import com.example.bankcards.dto.UserReadDto;
import com.example.bankcards.entity.User;
import com.example.bankcards.enumpack.Role;
import com.example.bankcards.filter.UserFilter;
import com.example.bankcards.service.UserService;
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

import java.time.LocalDate;
import java.util.ArrayList;

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
public class UserRestControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserRestController userRestController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(userRestController).build();
    }

    @Test
    void findAll() throws Exception {
        UserFilter filter = UserFilter.builder().build();
        PageRequest pageable = PageRequest.of(0, 20);
        Page<UserReadDto> pageUserReadDto = getPageUserReadDto();
        doReturn(pageUserReadDto).when(userService).findAll(filter, pageable);

        mockMvc.perform(get("/api/v1/users")
                        .queryParam("page", String.valueOf(0))
                        .queryParam("size", String.valueOf(20)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(userService).findAll(filter, pageable);
    }

    @Test
    void findByIdSuccess() throws Exception {
        UserReadDto userReadDto = getUserReadDto();
        doReturn(userReadDto).when(userService).findById(userReadDto.getId());

        mockMvc.perform(get("/api/v1/users/" + userReadDto.getId())
                        .queryParam("id", String.valueOf(userReadDto.getId()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.valueOf(userReadDto.getId())))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        verify(userService).findById(userReadDto.getId());
    }

    @Test
    void createSuccess() throws Exception {
        UserCreateEditDto userCreateEditDto = getUserCreateEditDto();
        UserReadDto userReadDto = getUserReadDto();
        doReturn(userReadDto).when(userService).create(userCreateEditDto);

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .writeValueAsBytes(userCreateEditDto)))
                .andExpect(status().isCreated());

        verify(userService).create(userCreateEditDto);
    }

    @Test
    void createFailedIfNoValidValue() throws Exception {
        UserCreateEditDto userCreateEditDto = UserCreateEditDto.builder().build();

        MvcResult mvcResult = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsBytes(userCreateEditDto)))
                .andExpect(status().is4xxClientError())
                .andReturn();
        Exception actualException = mvcResult.getResolvedException();

        assertNotNull(actualException);
        assertInstanceOf(MethodArgumentNotValidException.class, actualException);
        verifyNoInteractions(userService);
    }

    @Test
    void updateSuccess() throws Exception {
        UserCreateEditDto userCreateEditDto = getUserCreateEditDto();
        UserReadDto userReadDto = getUserReadDto();
        doReturn(userReadDto).when(userService).update(userReadDto.getId(), userCreateEditDto);

        mockMvc.perform(put("/api/v1/users/" + userReadDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper()
                                .registerModule(new JavaTimeModule())
                                .writeValueAsBytes(userCreateEditDto)))
                .andExpect(status().isOk());

        verify(userService).update(userReadDto.getId(), userCreateEditDto);
    }

    @Test
    void updateFailedIfNoValidValue() throws Exception {
        UserCreateEditDto userCreateEditDto = UserCreateEditDto.builder().build();
        User user = getUser();

        MvcResult mvcResult = mockMvc.perform(put("/api/v1/users/" + user.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsBytes(userCreateEditDto)))
                .andExpect(status().is4xxClientError())
                .andReturn();
        Exception actualException = mvcResult.getResolvedException();

        assertNotNull(actualException);
        assertInstanceOf(MethodArgumentNotValidException.class, actualException);
        verifyNoInteractions(userService);
    }

    @Test
    void deleteSuccess() throws Exception {
        User user = getUser();
        doReturn(true).when(userService).delete(user.getId());

        mockMvc.perform(delete("/api/v1/users/" + user.getId()))
                .andExpect(status().isNoContent());

        verify(userService).delete(user.getId());
    }

    @Test
    void deleteFailedIfUserNotFound() throws Exception {
        User user = getUser();
        doReturn(false).when(userService).delete(user.getId());

        MvcResult mvcResult = mockMvc.perform(delete("/api/v1/users/" + user.getId()))
                .andExpect(status().is4xxClientError())
                .andReturn();
        Exception actualException = mvcResult.getResolvedException();

        assertNotNull(actualException);
        assertInstanceOf(ResponseStatusException.class, actualException);
        verify(userService).delete(user.getId());
    }

    private static User getUser() {
        return User.builder()
                .id(1L)
                .firstname("Ivan")
                .lastname("Ivanov")
                .passportNumber("11-11-111")
                .email("ivan@gmail.com")
                .role(Role.USER)
                .birthDate(LocalDate.of(2000, 11, 11))
                .passportNumber("HB111111")
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

    private static UserCreateEditDto getUserCreateEditDto() {
        return UserCreateEditDto.builder()
                .firstname("Ivan")
                .lastname("Ivanov")
                .phoneNumber("11-11-111")
                .email("ivan@gmail.com")
                .role(Role.USER)
                .birthDate(LocalDate.of(2000, 11, 11))
                .passportNumber("HB1111111")
                .password("1111")
                .build();
    }

    private static Page<UserReadDto> getPageUserReadDto() {
        java.util.List<UserReadDto> userReadDtoList = new ArrayList<>();
        UserReadDto ivan = UserReadDto.builder()
                .id(1L)
                .firstname("Ivan")
                .lastname("Ivanov")
                .phoneNumber("11-11-111")
                .email("ivan@gmail.com")
                .role(Role.USER)
                .birthDate(LocalDate.of(2000, 11, 11))
                .passportNumber("HB1111111")
                .build();
        UserReadDto petr = UserReadDto.builder()
                .id(2L)
                .firstname("Petr")
                .lastname("Petrov")
                .passportNumber("22-22-222")
                .email("petr@gmail.com")
                .role(Role.ADMIN)
                .birthDate(LocalDate.of(2005, 12, 22))
                .passportNumber("HB22222")
                .build();
        userReadDtoList.add(ivan);
        userReadDtoList.add(petr);
        return new PageImpl<>(userReadDtoList);
    }
}

