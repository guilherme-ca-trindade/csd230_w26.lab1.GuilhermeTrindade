package csd230.lab1.config;

import csd230.lab1.auth.JwtAuthorizationFilter;
import csd230.lab1.services.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Lecture 2.6: the rules. The filter chain runs before any controller, so a request that
 * breaks a rule here never reaches BookController at all.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthorizationFilter jwtAuthorizationFilter;   // Lecture 2.10

    public WebSecurityConfig(CustomUserDetailsService userDetailsService,
                             JwtAuthorizationFilter jwtAuthorizationFilter) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthorizationFilter = jwtAuthorizationFilter;
    }

    /**
     * Lecture 2.10: AuthController needs this to check the credentials sent to /login. It is
     * the same manager the form login uses, so both paths go through DaoAuthenticationProvider
     * and BCrypt.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                // order matters - specific rules first, the catch-all last

                // 1. public: login and register have to work before anyone is logged in.
                //    /error has to be public too, or the 404 from BookNotFoundAdvice is
                //    swallowed and replaced by a redirect to /login.
                //    Lecture 2.10 drops "/api/rest/**" from this list: the API is no longer
                //    wide open, it is reached with a token instead.
                .requestMatchers("/h2-console/**", "/login", "/register",
                        "/css/**", "/js/**", "/error").permitAll()

                // Lecture 2.8: the Swagger pages are the API documentation, so they have
                // to be readable before anyone signs in
                .requestMatchers("/v3/api-docs/**", "/v3/api-docs.yaml",
                        "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // Lecture 2.10: the one API route that must stay open - a client cannot
                // present a token before it has asked for one
                .requestMatchers("/api/rest/auth/**").permitAll()

                // 2. admin only: the book CRUD. A USER hitting these gets 403 Forbidden
                .requestMatchers("/books/add", "/books/edit/**", "/books/delete/**").hasRole("ADMIN")

                // Lecture 2.11: TEMPORARY - the React frontend has no login screen yet, so
                // it cannot send a token. This opens the API for the classroom exercises
                // only and shadows the two role rules below it. Lecture 2.12 removes this
                // line and the frontend starts sending the Bearer token instead.
                .requestMatchers("/api/rest/**").permitAll()

                // Lecture 2.10: reading the API needs any signed-in role, writing to it
                // needs ADMIN. The GET rule has to come first - the one below it matches
                // every method, so it would otherwise swallow the reads too.
                .requestMatchers(HttpMethod.GET, "/api/rest/**").hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/rest/**").hasRole("ADMIN")

                // 3. everything else just needs a login - secure by default
                .anyRequest().authenticated()
            )
            // Lecture 2.10: an API client wants a status code it can branch on, not the
            // login page. A browser still gets the redirect it expects.
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    if (request.getRequestURI().startsWith("/api/rest/")) {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    } else {
                        response.sendRedirect("/login");
                    }
                })
            )
            // Lecture 2.10: runs before the form-login filter, so a request carrying a token
            // is already authenticated by the time the route rules are checked
            .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
            .formLogin((form) -> form
                .loginPage("/login")              // use LoginController's page, not Spring's built-in one
                .defaultSuccessUrl("/books", true)
                .permitAll()
            )
            .logout((logout) -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        // the H2 console renders in frames and sends no CSRF token, so it needs both
        // of these or it comes back as a blank page
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
        // Lecture 2.9: same reasoning for /api/rest/** - a REST client holds no session
        // and so has no CSRF token to send. The browser routes keep the normal protection.
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**", "/api/rest/**"));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ties the two halves together: userDetailsService finds the row,
    // passwordEncoder checks the hash against what was typed
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
