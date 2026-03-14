package com.taskflow.taskflow_backend.security;

import com.taskflow.taskflow_backend.repository.TokenBlocklistRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlocklistRepository tokenBlocklistRepository; // ✅ ADD

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        String email;
        try {
            email = jwtService.extractEmail(token);
        } catch (Exception e) {
            filterChain.doFilter(request, response);
            return;
        }

        // ✅ CHECK BLOCKLIST — if jti is revoked, reject immediately
        try {
            String jti = jwtService.extractJti(token);
            if (jti != null && tokenBlocklistRepository.existsByJti(jti)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write(
                    "{\"status\":401,\"message\":\"Session has been revoked\"}");
                return;
            }
        } catch (Exception e) {
            // ✅ Don't return — just skip blocklist check and continue
            System.out.println("JTI check skipped: " + e.getMessage());
        }

        if (email != null
                && SecurityContextHolder.getContext()
                        .getAuthentication() == null) {

            UserDetails userDetails;
            try {
                userDetails = userDetailsService.loadUserByUsername(email);
            } catch (Exception e) {
                filterChain.doFilter(request, response);
                return;
            }

            if (jwtService.isTokenValid(token, userDetails)) {

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                authToken.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request));

                SecurityContextHolder.getContext()
                        .setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}