package com.messageforge.config;

import com.messageforge.model.User;
import com.messageforge.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtTokenProvider.extractEmail(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional<User> userOpt = userRepository.findByEmailAndIsActiveTrue(username);

            if (userOpt.isPresent() && jwtTokenProvider.validateToken(jwt)) {
                User user = userOpt.get();
                
                // Create principal details with role-based authority
                List<org.springframework.security.core.GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + user.getRole()));
                
                // Store the User entity as the primary principal object for controller injection
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        user, null, authorities
                );
                
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        chain.doFilter(request, response);
    }
}
