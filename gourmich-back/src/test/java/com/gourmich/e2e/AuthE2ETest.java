package com.gourmich.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gourmich.dto.LoginRequest;
import com.gourmich.dto.RegisterUserDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=true"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureMockMvc
class AuthE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullAuthFlow_ShouldSucceed() throws Exception {
        RegisterUserDTO register = new RegisterUserDTO();
        register.setUsername("e2euser");
        register.setPassword("password123");
        register.setEmail("e2euser@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andDo(print())
                .andExpect(status().isOk());

        LoginRequest login = new LoginRequest();
        login.setUsername("e2euser");
        login.setPassword("password123");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();
        assertThat(token).isNotBlank();

        mockMvc.perform(get("/api/recipes")
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void login_ShouldFail_WithInvalidCredentials() throws Exception {
        RegisterUserDTO register = new RegisterUserDTO();
        register.setUsername("e2euser");
        register.setPassword("password123");
        register.setEmail("e2euser@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        LoginRequest badLogin = new LoginRequest();
        badLogin.setUsername("e2euser");
        badLogin.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badLogin)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_ShouldFail_WithInvalidData() throws Exception {
        RegisterUserDTO invalid = new RegisterUserDTO();
        invalid.setUsername("");
        invalid.setPassword("short");
        invalid.setEmail("invalidemail");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_ShouldFail_IfUsernameExists() throws Exception {
        RegisterUserDTO register = new RegisterUserDTO();
        register.setUsername("e2euser");
        register.setPassword("password123");
        register.setEmail("e2euser@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        RegisterUserDTO duplicate = new RegisterUserDTO();
        duplicate.setUsername("e2euser");
        duplicate.setPassword("password123");
        duplicate.setEmail("other@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andDo(print())
                .andExpect(status().isConflict());
    }

    @Test
    void login_ShouldFail_UsernameNotFound() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsername("nonexistent");
        login.setPassword("password");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_ShouldFail_WithoutToken() throws Exception {
        mockMvc.perform(get("/api/favorites"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_ShouldFail_WithInvalidToken() throws Exception {
        mockMvc.perform(get("/api/favorites")
                        .header("Authorization", "Bearer invalidtoken"))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }
}