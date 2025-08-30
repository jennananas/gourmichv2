package com.gourmich.service;

import com.gourmich.models.UserPrincipal;
import com.gourmich.models.Users;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JWTServiceTest {

    private JWTService jwtService;
    private Users user;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
        jwtService.init();

        user = new Users();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("testuser@example.com");
        user.setPassword("secret");
    }

    // ------------------- init -------------------
    @Test
    void init_ShouldGenerateKey_WhenSecretKeyIsEmpty() {
        JWTService service = new JWTService();
        service.secretKey = "";
        service.init();
        assertNotNull(service.getKey(), "La clé doit être générée dynamiquement");
    }

    @Test
    void init_ShouldUseProvidedKey_WhenSecretKeyIsSet() {
        JWTService service = new JWTService();
        SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        service.secretKey = Base64.getEncoder().encodeToString(key.getEncoded());
        service.init();
        assertNotNull(service.getKey(), "La clé doit être décodée depuis le secret");
    }

    // ------------------- generateToken -------------------
    @Test
    void generateToken_ShouldReturnNonEmptyToken() {
        String token = jwtService.generateToken(user.getUsername());
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(user.getUsername(), jwtService.extractUserName(token));
    }

    // ------------------- isTokenExpired -------------------
    @Test
    void isTokenExpired_ValidToken_ShouldReturnFalse() {
        String token = jwtService.generateToken(user.getUsername());
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_InvalidToken_ShouldReturnTrue() {
        String invalidToken = "invalid.token.value";
        assertTrue(jwtService.isTokenExpired(invalidToken));
    }

    @Test
    void isTokenExpired_AlreadyExpiredToken_ShouldReturnTrue() throws InterruptedException {
        String token = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 50))
                .signWith(jwtService.getKey())
                .compact();

        Thread.sleep(100);
        assertTrue(jwtService.isTokenExpired(token));
    }

    // ------------------- validateToken -------------------
    @Test
    void validateToken_ValidToken_ShouldReturnTrue() {
        String token = jwtService.generateToken(user.getUsername());
        UserPrincipal principal = new UserPrincipal(user);
        assertTrue(jwtService.validateToken(token, principal));
    }

    @Test
    void validateToken_ExpiredOrInvalidToken_ShouldReturnFalse() {
        String expiredToken = Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis() - 1000 * 60))
                .setExpiration(new Date(System.currentTimeMillis() - 1000 * 30))
                .signWith(jwtService.getKey())
                .compact();

        UserPrincipal principal = new UserPrincipal(user);
        assertFalse(jwtService.validateToken(expiredToken, principal));

        String invalidToken = "invalid.token.value";
        assertFalse(jwtService.validateToken(invalidToken, principal));
    }

    @Test
    void validateToken_ValidTokenAndMatchingUser_ShouldReturnTrue() {
        String token = jwtService.generateToken(user.getUsername());

        UserPrincipal principal = new UserPrincipal(user);

        assertTrue(jwtService.validateToken(token, principal));
    }

    @Test
    void isTokenExpired_CustomShortLivedToken_ShouldReturnTrue() throws InterruptedException {
        String token = jwtService.generateTokenWithCustomExpiration(user.getUsername(), 50);

        Thread.sleep(100);

        assertTrue(jwtService.isTokenExpired(token));
    }

    // ------------------- extractUserName -------------------
    @Test
    void extractUserName_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken(user.getUsername());
        assertEquals(user.getUsername(), jwtService.extractUserName(token));
    }
}