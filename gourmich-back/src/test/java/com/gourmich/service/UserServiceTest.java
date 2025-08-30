package com.gourmich.service;

import com.gourmich.dto.AuthResponse;
import com.gourmich.dto.LoginRequest;
import com.gourmich.models.Users;
import com.gourmich.repo.UserRepository;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    private AutoCloseable mocks;

    @BeforeEach
    void setup() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    // ------------------- register -------------------
    @Test
    void register_ShouldEncodePasswordAndSave() {
        Users user = new Users();
        user.setUsername("testuser");
        user.setPassword("plainpassword");

        when(userRepository.save(any(Users.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Users saved = userService.register(user);

        assertNotEquals("plainpassword", saved.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("plainpassword", saved.getPassword()));
        assertEquals("testuser", saved.getUsername());
        verify(userRepository, times(1)).save(saved);
    }

    // ------------------- login -------------------
    @Test
    void login_ValidCredentials_ShouldReturnToken() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getName()).thenReturn("testuser");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        when(jwtService.generateToken("testuser")).thenReturn("fake-jwt-token");

        ResponseEntity<Object> response = userService.login(request);

        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof AuthResponse);
        assertEquals("fake-jwt-token", ((AuthResponse) response.getBody()).getToken());
    }

    @Test
    void login_InvalidCredentials_ShouldReturnUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad creds"));

        ResponseEntity<Object> response = userService.login(request);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    void login_AuthenticationException_ShouldReturnUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new AuthenticationServiceException("Fail"));

        ResponseEntity<Object> response = userService.login(request);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Authentication failed", response.getBody());
    }

    @Test
    void login_AuthenticationNotAuthenticated_ShouldReturnUnauthorized() {
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        ResponseEntity<Object> response = userService.login(request);

        assertEquals(401, response.getStatusCodeValue());
        assertEquals("Authentication failed", response.getBody());
    }

    // ------------------- emailExists / usernameExists -------------------
    @Test
    void emailExists_ShouldReturnTrueOrFalse() {
        when(userRepository.existsByEmail("a@b.com")).thenReturn(true);
        assertTrue(userService.emailExists("a@b.com"));

        when(userRepository.existsByEmail("c@d.com")).thenReturn(false);
        assertFalse(userService.emailExists("c@d.com"));
    }

    @Test
    void usernameExists_ShouldReturnTrueOrFalse() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);
        assertTrue(userService.usernameExists("testuser"));

        when(userRepository.existsByUsername("otheruser")).thenReturn(false);
        assertFalse(userService.usernameExists("otheruser"));
    }
}