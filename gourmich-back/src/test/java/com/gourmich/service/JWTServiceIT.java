package com.gourmich.service;

import com.gourmich.models.UserPrincipal;
import com.gourmich.models.Users;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                "spring.datasource.driverClassName=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.jpa.show-sql=true"
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JWTServiceIT {

    @Autowired
    private JWTService jwtService; // Spring gère le bean

    private Users user;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
        jwtService.init(); // initialise la clé dynamique si secretKey vide

        user = new Users();
        user.setId(1L);
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword(new BCryptPasswordEncoder().encode("secret"));
    }


    @Test
    void generateToken_WithSecret_ShouldReturnValidToken() {
        String token = jwtService.generateToken("testuser");
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("testuser", jwtService.extractUserName(token));
    }

    @Test
    void generateToken_InvalidToken_ShouldFailValidation() {
        String token = jwtService.generateToken("testuser");

        String invalidToken = token + "abc";

        Users user = new Users();
        user.setUsername("testuser");
        UserPrincipal principal = new UserPrincipal(user);

        assertFalse(jwtService.validateToken(invalidToken, principal));
        assertThrows(Exception.class, () -> jwtService.extractUserName(invalidToken));
    }

    @Test
    void getKey_ShouldReturnGeneratedKey_WhenSecretKeyIsEmpty() {
        jwtService.secretKey = "";
        jwtService.init();

        SecretKey key = jwtService.getKey();

        assertNotNull(key, "La clé générée dynamiquement ne doit pas être null");
        assertEquals(256 / 8, key.getEncoded().length, "La longueur de la clé doit être de 256 bits");
    }

    @Test
    void getKey_ShouldReturnKeyFromSecret_WhenSecretKeyProvided() {
        byte[] randomBytes = new byte[32]; // 256 bits
        Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256).getEncoded();
        for (int i = 0; i < 32; i++) randomBytes[i] = (byte)i;
        String base64Key = Base64.getEncoder().encodeToString(randomBytes);

        jwtService.secretKey = base64Key;
        jwtService.init();

        SecretKey key = jwtService.getKey();

        assertNotNull(key, "La clé créée depuis secretKey ne doit pas être null");
        assertArrayEquals(randomBytes, key.getEncoded(), "La clé doit correspondre à la valeur décodée de secretKey");
    }

    @Test
    void extractUserName_ValidToken_ShouldReturnUsername() {
        String token = jwtService.generateToken("testuser");
        String username = jwtService.extractUserName(token);

        assertNotNull(username);
        assertEquals("testuser", username);
    }

    @Test
    void extractUserName_InvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token.parts";

        assertThrows(Exception.class, () -> jwtService.extractUserName(invalidToken));
    }

    @Test
    void extractClaim_ValidToken_ShouldReturnClaim() {
        String token = jwtService.generateToken("testuser");

        String username = jwtService.extractClaim(token, Claims::getSubject);
        assertEquals("testuser", username);
    }

    @Test
    void extractClaim_InvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token.parts";

        assertThrows(Exception.class, () ->
                jwtService.extractClaim(invalidToken, Claims::getSubject)
        );
    }

    @Test
    void extractAllClaims_ValidToken_ShouldReturnClaims() {
        String token = jwtService.generateToken("testuser");

        Claims claims = jwtService.extractAllClaims(token);
        assertNotNull(claims);
        assertEquals("testuser", claims.getSubject());
    }

    @Test
    void extractAllClaims_InvalidToken_ShouldThrowException() {
        String invalidToken = "invalid.token.parts";

        assertThrows(Exception.class, () -> jwtService.extractAllClaims(invalidToken));
    }

    @Test
    void validateToken_ValidTokenAndMatchingUser_ShouldReturnTrue() {
        UserDetails fakeUser = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("password")
                .authorities(new java.util.ArrayList<>())
                .build();

        String token = jwtService.generateToken(fakeUser.getUsername());

        assertTrue(jwtService.validateToken(token, fakeUser));
    }

    @Test
    void validateToken_InvalidUsername_ShouldReturnFalse() {
        UserDetails user = org.springframework.security.core.userdetails.User
                .withUsername("user1")
                .password("password")
                .authorities(new java.util.ArrayList<>())
                .build();

        String token = jwtService.generateToken("otheruser");

        assertFalse(jwtService.validateToken(token, user));
    }

    @Test
    void validateToken_ExpiredToken_ShouldReturnFalse() throws InterruptedException {
        UserDetails user = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("password")
                .authorities(new java.util.ArrayList<>())
                .build();

        JWTService shortLivedJwtService = new JWTService() {
            @Override
            public String generateToken(String username) {
                return io.jsonwebtoken.Jwts.builder()
                        .setSubject(username)
                        .setIssuedAt(new java.util.Date(System.currentTimeMillis()))
                        .setExpiration(new java.util.Date(System.currentTimeMillis() + 100)) // 100 ms
                        .signWith(Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256))
                        .compact();
            }
        };
        shortLivedJwtService.init();

        String token = shortLivedJwtService.generateToken(user.getUsername());

        Thread.sleep(200);

        assertFalse(shortLivedJwtService.validateToken(token, user));
    }

    @Test
    void validateToken_InvalidTokenFormat_ShouldReturnFalse() {
        UserDetails user = org.springframework.security.core.userdetails.User
                .withUsername("testuser")
                .password("password")
                .authorities(new java.util.ArrayList<>())
                .build();

        String invalidToken = "abc.def.ghi";

        assertFalse(jwtService.validateToken(invalidToken, user));
    }

    @Test
    void isTokenExpired_NotExpired_ShouldReturnFalse() {
        String token = jwtService.generateToken("testuser");
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void isTokenExpired_AlreadyExpired_ShouldReturnTrue() throws InterruptedException {
        JWTService shortLivedJwtService = new JWTService();
        shortLivedJwtService.init();

        String token = io.jsonwebtoken.Jwts.builder()
                .setSubject("testuser")
                .setIssuedAt(new java.util.Date(System.currentTimeMillis()))
                .setExpiration(new java.util.Date(System.currentTimeMillis() + 100)) // 100 ms
                .signWith(shortLivedJwtService.getKey())
                .compact();

        Thread.sleep(200);

        assertTrue(shortLivedJwtService.isTokenExpired(token));
    }

    @Test
    void extractExpiration_ValidToken_ShouldReturnFutureDate() {
        String token = jwtService.generateToken("testuser");
        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration, "La date d'expiration ne doit pas être null");
        assertTrue(expiration.after(new Date()), "La date d'expiration doit être dans le futur");
    }

    @Test
    void extractExpiration_AlreadyExpiredToken_ShouldReturnPastDate() throws InterruptedException {
        String token = jwtService.generateTokenWithCustomExpiration("testuser", 100); // 100ms

        Thread.sleep(200);

        Date expiration = jwtService.extractExpiration(token);

        assertNotNull(expiration, "La date d'expiration ne doit pas être null");
        assertTrue(expiration.before(new Date()), "La date d'expiration doit être passée");
    }



}