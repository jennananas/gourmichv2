package com.gourmich.service;

import com.gourmich.models.UserPrincipal;
import com.gourmich.models.Users;
import com.gourmich.repo.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
        }
)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CustomUserDetailsServiceIT {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private Users user;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user = new Users();
        user.setUsername("john");
        user.setEmail("john@example.com");
        user.setPassword(passwordEncoder.encode("secret"));
        userRepository.save(user);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserPrincipal_WhenUserExists() {
        UserDetails userDetails = userDetailsService.loadUserByUsername("john");

        assertNotNull(userDetails);
        assertTrue(userDetails instanceof UserPrincipal);
        assertEquals("john", userDetails.getUsername());
    }

    @Test
    void loadUserEntityByUsername_ShouldReturnUserEntity_WhenUserExists() {
        Users found = userDetailsService.loadUserEntityByUsername("john");

        assertNotNull(found);
        assertEquals("john", found.getUsername());
        assertEquals("john@example.com", found.getEmail());
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("ghost"));
    }

    @Test
    void loadUserEntityByUsername_ShouldThrowException_WhenUserDoesNotExist() {
        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserEntityByUsername("ghost"));
    }
}