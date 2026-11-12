package csd230.lab1.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Lecture 2.10: checks the Bearer token before the controller runs.
 *
 * OncePerRequestFilter guarantees one pass per request dispatch. The filter establishes who the
 * caller is; it does not decide what they may do - the route rules in WebSecurityConfig still
 * make that call, so a valid token with the wrong role gets 403 rather than 401.
 */
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // Built here rather than injected: Boot 4 autoconfigures a Jackson 3 mapper, while jjwt
    // brings Jackson 2 along, and this filter only needs a mapper for its own error body.
    private final ObjectMapper mapper = new ObjectMapper();

    public JwtAuthorizationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String accessToken = jwtUtil.resolveToken(request);

            // No token is not an error: form login and the public routes still have their say.
            if (accessToken == null) {
                filterChain.doFilter(request, response);
                return;
            }

            Claims claims = jwtUtil.resolveClaims(request);

            if (claims != null && jwtUtil.validateClaims(claims)) {
                String username = claims.getSubject();

                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) claims.get("roles");

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (roles != null) {
                    authorities = roles.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
                }

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(username, "", authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            // An expired or tampered token stops here with JSON, not a Whitelabel error page.
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("message", "Authentication Error");
            errorDetails.put("details", e.getMessage());

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            mapper.writeValue(response.getWriter(), errorDetails);
            return;
        }

        filterChain.doFilter(request, response);
    }
}
