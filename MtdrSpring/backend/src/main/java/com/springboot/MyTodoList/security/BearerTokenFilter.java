package com.springboot.MyTodoList.security;

import com.springboot.MyTodoList.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class BearerTokenFilter extends OncePerRequestFilter {
    private final JwtService jwtService;

    public BearerTokenFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip token check for non-API paths and auth endpoints
        String path = request.getServletPath();
        if (!path.startsWith("/api/") || 
            path.contains("/request-code") || 
            path.contains("/validate-code")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Try to get token from cookie first
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("auth_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // If no cookie, try Authorization header
        if (token == null) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            }
        }

        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Missing authentication token");
            return;
        }

        try {
            Claims claims = jwtService.validateToken(token);
            Integer userId = claims.get("userId", Integer.class);
            Integer projectId = claims.get("projectId", Integer.class);

            // Create authentication with user and project ID
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId, null, AuthorityUtils.createAuthorityList("ROLE_USER"));
            authentication.setDetails(projectId);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid or expired token");
        }
    }
}