package com.gourmich.config;

import com.gourmich.models.UserPrincipal;
import com.gourmich.models.Users;
import com.gourmich.service.CustomUserDetailsService;
import com.gourmich.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    @Autowired
    JWTService jwtService;

    @Autowired
    ApplicationContext context;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getServletPath();
        String method = request.getMethod();
        logger.info("Incoming request: {} {}", method, path);

        boolean requiresAuth =
                path.startsWith("/api/favorites") ||
                        (path.startsWith("/api/recipes") &&
                                !( (path.equals("/api/recipes") && method.equalsIgnoreCase("GET")) ||
                                        (path.equals("/api/recipes/latest") && method.equalsIgnoreCase("GET"))
                                )
                        );

        if (!requiresAuth) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        String token = authHeader.substring(7);
        String username;
        try {
            username = jwtService.extractUserName(token);
        } catch (Exception e) {
            logger.warn("Invalid JWT token", e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                Users userEntity = context.getBean(CustomUserDetailsService.class)
                        .loadUserEntityByUsername(username);

                UserPrincipal principal = new UserPrincipal(userEntity);

                if (jwtService.validateToken(token, principal)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Authentication set in SecurityContext for user: {}", username);
                } else {
                    logger.warn("Invalid JWT token for user: {}", username);
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                    return;
                }
            } catch (UsernameNotFoundException e) {
                logger.warn("User not found for token: {}", username);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}