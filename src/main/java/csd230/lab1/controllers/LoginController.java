package csd230.lab1.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Lecture 2.6: only serves the page. The POST to /login is handled by Spring Security's
 * filter chain, not by a method here.
 */
@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
