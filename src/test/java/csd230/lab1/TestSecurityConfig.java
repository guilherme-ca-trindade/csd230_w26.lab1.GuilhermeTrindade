package csd230.lab1;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Lecture 2.6: @DataJpaTest only loads the JPA slice, so WebSecurityConfig is filtered out
 * and its PasswordEncoder bean goes with it. Application now takes that bean in its
 * constructor, so without this the repository tests fail with UnsatisfiedDependencyException
 * before a single query runs. @SpringBootTest loads the real config and does not need it.
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
