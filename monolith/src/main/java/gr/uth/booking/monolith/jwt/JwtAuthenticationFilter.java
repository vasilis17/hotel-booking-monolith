package gr.uth.booking.monolith.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Pairno to Token
        String token = jwtTokenProvider.resolveToken(request);

        // Elegxos an to Token iparxei kai einai egkiro
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // Pairno to Authentication
            Authentication auth = jwtTokenProvider.getAuthentication(token);

            // To vazo sto SecurityContext
            if (auth != null) {
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // Sinexizo me to epomeno filtro
        filterChain.doFilter(request, response);
    }
}