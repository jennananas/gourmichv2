package com.gourmich.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserPrincipalTest {

    private UserPrincipal principal;

    @BeforeEach
    void setUp() {
        Users user = new Users("john@example.com", "john", "secret");
        principal = new UserPrincipal(user);
    }

    @Test
    void getUsername_ShouldReturnUserUsername() {
        assertEquals("john", principal.getUsername());
    }

    @Test
    void getPassword_ShouldReturnUserPassword() {
        assertEquals("secret", principal.getPassword());
    }

    @Test
    void getEmail_ShouldReturnUserEmail() {
        assertEquals("john@example.com", principal.getEmail());
    }

    @Test
    void getAuthorities_ShouldReturnSingleUserRole() {
        Collection<? extends GrantedAuthority> authorities = principal.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("USER")));
    }
}