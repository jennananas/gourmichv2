package com.gourmich.models;

import com.gourmich.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UsersTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void constructor_ShouldInitializeFields() {
        Users user = new Users("test@example.com", "john", "secret");

        assertEquals("test@example.com", user.getEmail());
        assertEquals("john", user.getUsername());
        assertEquals("secret", user.getPassword());
    }

    @Test
    void gettersAndSetters_ShouldWork() {
        Users user = new Users();
        user.setEmail("test@example.com");
        user.setUsername("john");
        user.setPassword("secret");

        assertEquals("test@example.com", user.getEmail());
        assertEquals("john", user.getUsername());
        assertEquals("secret", user.getPassword());
    }

    @Test
    void saveUser_WithNullUsername_ShouldFail() {
        Users user = new Users();
        user.setEmail("test@example.com");
        user.setPassword("secret");
        user.setUsername(null);

        assertThrows(Exception.class, () -> userRepository.saveAndFlush(user));
    }

    @Test
    void saveUser_WithDuplicateUsername_ShouldFail() {
        Users user1 = new Users("a@example.com", "john", "secret");
        userRepository.saveAndFlush(user1);

        Users user2 = new Users("b@example.com", "john", "secret");
        assertThrows(Exception.class, () -> userRepository.saveAndFlush(user2));
    }

}