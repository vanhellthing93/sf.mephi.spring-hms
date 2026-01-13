package sf.mephi.booking.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import sf.mephi.booking.config.BaseControllerTest;
import sf.mephi.booking.dto.request.AuthRequest;
import sf.mephi.booking.dto.request.RegisterRequest;
import sf.mephi.booking.dto.response.AuthResponse;
import sf.mephi.booking.service.UserService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest extends BaseControllerTest {

    @MockitoBean
    private UserService userService;

    @Test
    void register_ShouldReturnToken_WhenValidRequest() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("newuser")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .username("newuser")
                .role("USER")
                .build();

        when(userService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.role").value("USER"));

        verify(userService).register(any(RegisterRequest.class));
    }


    @Test
    void login_ShouldReturnToken_WhenValidCredentials() throws Exception {
        AuthRequest request = AuthRequest.builder()
                .username("testuser")
                .password("password123")
                .build();

        AuthResponse response = AuthResponse.builder()
                .token("jwt-token")
                .username("testuser")
                .role("USER")
                .build();

        when(userService.authenticate(any(AuthRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"));

        verify(userService).authenticate(any(AuthRequest.class));
    }


    @Test
    void register_ShouldReturn400_WhenValidationFails() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .username("")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any());
    }

    @Test
    void login_ShouldReturn400_WhenMissingPassword() throws Exception {
        AuthRequest request = AuthRequest.builder()
                .username("testuser")
                .password(null)
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).authenticate(any());
    }
}