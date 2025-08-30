package com.gourmich.service;

import com.gourmich.dto.AuthResponse;
import com.gourmich.dto.LoginRequest;
import com.gourmich.exception.UserAlreadyExistsException;
import com.gourmich.models.Users;
import com.gourmich.repo.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository repo;

    @Autowired
    private JWTService jwtService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Transactional
    public Users register(Users user) {
        if (usernameExists(user.getUsername())) {
            throw new UserAlreadyExistsException("Username already taken");
        }
        user.setPassword(new BCryptPasswordEncoder(12).encode(user.getPassword()));
        return repo.save(user);
    }

    public ResponseEntity<Object> login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            if (authentication.isAuthenticated()) {
                String authenticatedUsername = authentication.getName();
                String token = jwtService.generateToken(authenticatedUsername);
                return ResponseEntity.ok(new AuthResponse(token));
            }
            else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
            }
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
        }
    }

    public boolean emailExists(String email) {
        return repo.existsByEmail(email);
    }

    public boolean usernameExists(String username) {
        return repo.existsByUsername(username);
    }
}
