package com.gourmich.service;

import com.gourmich.dto.AuthResponse;
import com.gourmich.dto.LoginRequest;
import com.gourmich.models.Users;
import com.gourmich.repo.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import static org.junit.jupiter.api.Assertions.*;

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
class UserServiceIT {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_ShouldSaveUserWithEncodedPassword() {
        Users user = new Users();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword("secret");

        Users saved = userService.register(user);

        assertNotNull(saved.getId());
        assertNotEquals("secret", saved.getPassword());
        assertTrue(passwordEncoder.matches("secret", saved.getPassword()));
    }

    @Test
    void login_ValidCredentials_ShouldReturnToken() {
        Users user = new Users();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword(passwordEncoder.encode("secret"));
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("secret");

        ResponseEntity<Object> response = userService.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof AuthResponse);

        AuthResponse body = (AuthResponse) response.getBody();
        assertNotNull(body.getToken());
        assertFalse(body.getToken().isBlank());
    }

    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorized() {
        // Préparer un utilisateur avec mot de passe connu
        Users user = new Users();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword(passwordEncoder.encode("correctpass")); // mot de passe encodé
        userRepository.save(user);

        // Tentative de login avec mauvais mot de passe
        LoginRequest request = new LoginRequest();
        request.setUsername("john");
        request.setPassword("wrongpass");

        ResponseEntity<Object> response = userService.login(request);

        // Vérifications
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody() instanceof String);
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    void emailExists_ShouldReturnTrueIfExistsAndFalseIfNot() {
        // Créer un utilisateur en base
        Users user = new Users();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword(passwordEncoder.encode("secret"));
        userRepository.save(user);

        // Cas où l'email existe
        assertTrue(userService.emailExists("john@example.com"));

        // Cas où l'email n'existe pas
        assertFalse(userService.emailExists("notfound@example.com"));
    }

    @Test
    void usernameExists_ShouldReturnTrueIfExistsAndFalseIfNot() {
        // Créer un utilisateur en base
        Users user = new Users();
        user.setUsername("alice");
        user.setEmail("alice@example.com");
        user.setPassword(passwordEncoder.encode("secret"));
        userRepository.save(user);

        // Cas où le username existe
        assertTrue(userService.usernameExists("alice"));

        // Cas où le username n'existe pas
        assertFalse(userService.usernameExists("bob"));
    }

}