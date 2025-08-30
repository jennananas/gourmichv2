package com.gourmich.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gourmich.dto.AuthResponse;
import com.gourmich.dto.LoginRequest;
import com.gourmich.dto.RegisterUserDTO;
import com.gourmich.exception.UserAlreadyExistsException;
import com.gourmich.models.Users;
import com.gourmich.service.JWTService;
import com.gourmich.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JWTService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    // ------------------- REGISTER -------------------
    @Test
    void testRegister_Success() throws Exception {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@test.com");
        dto.setPassword("password");

        Users returnedUser = new Users();
        returnedUser.setUsername("testuser");
        returnedUser.setEmail("test@test.com");

        when(userService.register(any(Users.class))).thenReturn(returnedUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("User registered"));
    }


    @Test
    void testRegister_UserAlreadyExists_ShouldReturnConflict() throws Exception {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setUsername("existinguser");
        dto.setEmail("existing@test.com");
        dto.setPassword("password");

        when(userService.register(any(Users.class)))
                .thenThrow(new UserAlreadyExistsException("User already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(content().string("User already exists"));
    }

    @Test
    void testRegister_InternalServerError_ShouldReturn500() throws Exception {
        RegisterUserDTO dto = new RegisterUserDTO();
        dto.setUsername("testuser");
        dto.setEmail("test@test.com");
        dto.setPassword("password");

        when(userService.register(any(Users.class)))
                .thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Server error"));
    }

    // ------------------- LOGIN -------------------
    @Test
    void testLogin_ValidCredentials_ShouldReturnOk() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        AuthResponse authResponse = new AuthResponse("fake-jwt-token");

        when(userService.login(any(LoginRequest.class)))
                .thenReturn(org.springframework.http.ResponseEntity.ok(authResponse));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("fake-jwt-token"));
    }

    @Test
    void testLogin_InvalidCredentials_ShouldReturn401() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wronguser");
        loginRequest.setPassword("wrongpassword");

        when(userService.login(any(LoginRequest.class)))
                .thenReturn(org.springframework.http.ResponseEntity.status(401).body("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid credentials"));
    }

    @Test
    void testLogin_InternalServerError_ShouldReturn500() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Server error"));
    }

    @Test
    void testLogin_BadCredentials_ShouldReturnUnauthorized() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("wronguser");
        loginRequest.setPassword("wrongpassword");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid username or password"));
    }

    // ------------------- CHECK EMAIL -------------------
    @Test
    void testCheckEmailExists_ShouldReturnTrue() throws Exception {
        when(userService.emailExists("test@test.com")).thenReturn(true);

        mockMvc.perform(get("/api/auth/check-email")
                        .param("email", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void testCheckEmailExists_InternalServerError_ShouldReturn500() throws Exception {
        when(userService.emailExists("test@test.com")).thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(get("/api/auth/check-email")
                        .param("email", "test@test.com"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.exists").value(false));
    }

    // ------------------- CHECK USERNAME -------------------
    @Test
    void testCheckUsernameExists_ShouldReturnTrue() throws Exception {
        when(userService.usernameExists("testuser")).thenReturn(true);

        mockMvc.perform(get("/api/auth/check-username")
                        .param("username", "testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exists").value(true));
    }

    @Test
    void testCheckUsernameExists_InternalServerError_ShouldReturn500() throws Exception {
        when(userService.usernameExists("testuser")).thenThrow(new RuntimeException("Unexpected"));

        mockMvc.perform(get("/api/auth/check-username")
                        .param("username", "testuser"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.exists").value(false));
    }
}