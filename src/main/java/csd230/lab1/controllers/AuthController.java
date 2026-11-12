package csd230.lab1.controllers;

import csd230.lab1.auth.JwtUtil;
import csd230.lab1.dtos.requests.LoginReq;
import csd230.lab1.dtos.responses.ErrorRes;
import csd230.lab1.dtos.responses.LoginRes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lecture 2.10: trades a username and password for a token.
 *
 * This is the one API route that has to stay public - a client cannot present a token before
 * it has one. The credentials are checked against the same database users and the same BCrypt
 * encoder the browser form login uses; JWT only changes how the identity travels afterwards.
 */
@RestController
@RequestMapping("/api/rest/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginReq loginReq) {
        try {
            // Runs the DaoAuthenticationProvider: CustomUserDetailsService loads the row,
            // BCryptPasswordEncoder compares the supplied password with the stored hash.
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginReq.getUsername(),
                            loginReq.getPassword())
            );

            String username = authentication.getName();

            // The authorities come from the database, so the token carries the real roles.
            String token = jwtUtil.createToken(username, authentication.getAuthorities());

            return ResponseEntity.ok(new LoginRes(username, token));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorRes(HttpStatus.BAD_REQUEST, "Invalid username or password"));
        }
    }
}
