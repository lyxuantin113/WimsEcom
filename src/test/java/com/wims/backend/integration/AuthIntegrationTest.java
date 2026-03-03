package com.wims.backend.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wims.backend.dto.request.LoginRequest;
import com.wims.backend.dto.request.RegisterRequest;
import com.wims.backend.entity.Role;
import com.wims.backend.entity.User;
import com.wims.backend.repository.RoleRepository;
import com.wims.backend.repository.UserRepository;
import com.wims.backend.service.infrastructure.RedisService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        roleRepository.deleteAll();

        Role userRole = new Role();
        userRole.setName("USER");
        userRole.setDescription("Customer");
        roleRepository.save(userRole);
    }

    @Test
    void registerAndLoginFlow() throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(
                "integrationuser",
                "password",
                "integration@example.com",
                "Integration User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000));

        LoginRequest loginRequest = new LoginRequest("integrationuser", "password");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1000))
                .andExpect(jsonPath("$.result.token").exists())
                .andExpect(jsonPath("$.result.refreshToken").exists())
                .andExpect(jsonPath("$.result.username").value("integrationuser"));
    }

    @Test
    void loginWithWrongPassword() throws Exception {
        User user = new User();
        user.setUsername("wrongpassuser");
        user.setPassword(passwordEncoder.encode("correctpassword"));
        user.setEmail("wrongpass@example.com");
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("wrongpassuser", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(986));
    }
}
