package csd230.lab1.services;

import csd230.lab1.entities.UserEntity;
import csd230.lab1.repositories.UserEntityRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Lecture 2.6: the translator between our users table and Spring Security. @Service means
 * Spring finds this bean on startup and uses it for every login attempt.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserEntityRepository userRepository;

    public CustomUserDetailsService(UserEntityRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // we hand over the stored hash and let Spring compare it to what was typed -
        // this class never checks a password itself.
        // roles() adds the "ROLE_" prefix, so "ADMIN" here matches hasRole("ADMIN")
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}
