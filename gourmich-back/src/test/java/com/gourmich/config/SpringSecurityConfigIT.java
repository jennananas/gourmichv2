package com.gourmich.config;

import com.gourmich.models.Users;
import com.gourmich.repo.UserRepository;
import com.gourmich.service.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=true"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SpringSecurityConfigIT {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private String baseUrl;

    private Users user;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
        userRepository.deleteAll();

        user = new Users();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword(passwordEncoder.encode("secret"));
        userRepository.save(user);
    }

    @Test
    void protectedEndpoint_WithValidToken_ShouldReturnOk() {
        String token = jwtService.generateToken(user.getUsername());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/api/favorites", HttpMethod.GET, entity, String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void protectedEndpoint_WithoutToken_ShouldReturnUnauthorized() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/favorites", String.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void publicEndpoint_WithoutToken_ShouldReturnOk() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/api/recipes", String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}