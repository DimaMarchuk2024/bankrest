package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginDto;
import com.example.bankcards.security.AccessToken;
import com.example.bankcards.security.AuthenticationToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
@ExtendWith(MockitoExtension.class)
class AuthenticationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationToken authenticationToken;

    @InjectMocks
    private AuthenticationController authenticationController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(authenticationController).build();
    }

    @Test
    void getTokenSuccess() throws Exception {
        LoginDto loginDto = LoginDto.builder()
                .email("test@gmail.com")
                .password("test")
                .build();
        AccessToken accessToken = new AccessToken("test.test.test");
        doReturn(accessToken).when(authenticationToken).authenticate(loginDto);

        mockMvc.perform(post("/api/v1/authentication")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsBytes(loginDto)))
                .andExpect(status().isOk());

        verify(authenticationToken).authenticate(loginDto);
    }
}