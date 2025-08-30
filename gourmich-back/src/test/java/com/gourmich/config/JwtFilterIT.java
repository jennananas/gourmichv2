package com.gourmich.config;

import com.gourmich.models.UserPrincipal;
import com.gourmich.models.Users;
import com.gourmich.service.CustomUserDetailsService;
import com.gourmich.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtFilterIT {

    private JwtFilter jwtFilter;
    private JWTService jwtService;
    private CustomUserDetailsService customUserDetailsService;

    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    private Users user;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
        jwtService.init();

        ApplicationContext context = mock(ApplicationContext.class);
        customUserDetailsService = mock(CustomUserDetailsService.class);
        when(context.getBean(CustomUserDetailsService.class)).thenReturn(customUserDetailsService);

        jwtFilter = new JwtFilter();
        jwtFilter.jwtService = jwtService;
        jwtFilter.context = context;

        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        user = new Users();
        user.setUsername("john");
        user.setPassword("secret");
    }

    @Test
    void doFilterInternal_ShouldSkipForPublicEndpoint() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/api/public");
        when(request.getMethod()).thenReturn("GET");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verifyNoMoreInteractions(response);
    }

    @Test
    void doFilterInternal_ShouldReturnUnauthorized_WhenAuthHeaderMissing() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/api/favorites");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldReturnUnauthorized_WhenTokenInvalid() throws ServletException, IOException {
        when(request.getServletPath()).thenReturn("/api/favorites");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid.token");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldReturnUnauthorized_WhenUserNotFound() throws ServletException, IOException {
        String token = jwtService.generateToken("ghostuser");

        when(request.getServletPath()).thenReturn("/api/favorites");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(customUserDetailsService.loadUserEntityByUsername("ghostuser"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenValidToken() throws ServletException, IOException {
        String token = jwtService.generateToken(user.getUsername());
        when(request.getServletPath()).thenReturn("/api/favorites");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(customUserDetailsService.loadUserEntityByUsername(user.getUsername())).thenReturn(user);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(user.getUsername(),
                ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldReturnUnauthorized_WhenTokenDoesNotValidate() throws ServletException, IOException {
        JWTService spyJwt = spy(jwtService);
        jwtFilter.jwtService = spyJwt;
        String token = jwtService.generateToken(user.getUsername());
        doReturn(false).when(spyJwt).validateToken(eq(token), any(UserPrincipal.class));

        when(request.getServletPath()).thenReturn("/api/favorites");
        when(request.getMethod()).thenReturn("GET");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(customUserDetailsService.loadUserEntityByUsername(user.getUsername())).thenReturn(user);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response, times(1)).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldCheckAuth_ForProtectedRecipeEndpoint() throws Exception {
        String token = jwtService.generateToken(user.getUsername());

        when(request.getServletPath()).thenReturn("/api/recipes/by-id/1");
        when(request.getMethod()).thenReturn("DELETE");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(customUserDetailsService.loadUserEntityByUsername(user.getUsername())).thenReturn(user);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(user.getUsername(),
                ((UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername());
        verify(filterChain, times(1)).doFilter(request, response);
    }


}