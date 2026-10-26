package csd230.lab1.config;

import csd230.lab1.services.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Lecture 2.6: the rules. The filter chain runs before any controller, so a request that
 * breaks a rule here never reaches BookController at all.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public WebSecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((requests) -> requests
                // order matters - specific rules first, the catch-all last

                // 1. public: login and register have to work before anyone is logged in.
                //    Lecture 2.9 adds the JSON API: a machine has no browser to show the
                //    login form to, and /error has to be public too or the 404 from
                //    BookNotFoundAdvice is swallowed and replaced by a redirect to /login.
                .requestMatchers("/h2-console/**", "/login", "/register",
                        "/css/**", "/js/**", "/api/rest/**", "/error").permitAll()

                // Lecture 2.8: the Swagger pages are the API documentation, so they have
                // to be readable before anyone signs in
                .requestMatchers("/v3/api-docs/**", "/v3/api-docs.yaml",
                        "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // 2. admin only: the book CRUD. A USER hitting these gets 403 Forbidden
                .requestMatchers("/books/add", "/books/edit/**", "/books/delete/**").hasRole("ADMIN")

                // 3. everything else just needs a login - secure by default
                .anyRequest().authenticated()
            )
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
