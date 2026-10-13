package csd230.lab1.controllers;

import csd230.lab1.entities.UserEntity;
import csd230.lab1.repositories.UserEntityRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Lab 3: sign-up. Until now the only accounts were the two seeded in Application, so
 * there was no way to get a new user into the system.
 */
@Controller
public class RegisterController {

    private final UserEntityRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterController(UserEntityRepository userRepository,
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new UserEntity());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute UserEntity user, Model model) {

        // username is unique in the database, so saving a duplicate would throw
        // DataIntegrityViolationException and show a 500 page - check first instead
        if (userRepository.findByUsername(user.getUsername()) != null) {
            model.addAttribute("error", "That username is already taken.");
            return "register";
        }

        // never store what was typed - hash it first
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // anyone signing up gets USER. ADMIN is only handed out by the seed in
        // Application, so nobody can make themselves an admin through this form
        user.setRole("USER");

        userRepository.save(user);

        // no cart is created here - CartController makes one the first time
        // this user opens their cart
        return "redirect:/login?registered";
    }
}
