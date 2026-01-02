package sf.mephi.hotel.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import sf.mephi.common.security.JwtUtil;
import sf.mephi.common.security.SecurityConstants;
import sf.mephi.common.util.CorrelationIdUtil;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Установка correlation ID
        String correlationId = request.getHeader(CorrelationIdUtil.CORRELATION_ID_HEADER);
        if (correlationId == null) {
            correlationId = CorrelationIdUtil.generateCorrelationId();
        }
        CorrelationIdUtil.setCorrelationId(correlationId);

        try {
            String authHeader = request.getHeader(SecurityConstants.JWT_HEADER);

            if (authHeader != null && authHeader.startsWith(SecurityConstants.JWT_PREFIX)) {
                String token = authHeader.substring(SecurityConstants.JWT_PREFIX.length());

                try {
                    String username = jwtUtil.extractUsername(token);
                    List<String> roles = jwtUtil.extractRoles(token);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        if (jwtUtil.isTokenValid(token, username)) {
                            List<SimpleGrantedAuthority> authorities = roles.stream()
                                    .map(SimpleGrantedAuthority::new)
                                    .collect(Collectors.toList());

                            UsernamePasswordAuthenticationToken authToken =
                                    new UsernamePasswordAuthenticationToken(
                                            username, null, authorities);

                            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authToken);

                            log.debug("User {} authenticated with roles: {}", username, roles);
                        }
                    }
                } catch (Exception e) {
                    log.error("JWT validation failed: {}", e.getMessage());
                }
            }

            filterChain.doFilter(request, response);

        } finally {
            CorrelationIdUtil.clearCorrelationId();
        }
    }
}
