package csd230.lab1.entities;

import jakarta.persistence.*;

/**
 * Lecture 2.6: the login credentials. Spring Security does not read this class directly -
 * CustomUserDetailsService translates it into the UserDetails object security understands.
 */
@Entity
@Table(name = "users")   // "user" is a reserved SQL word, same problem as "order"
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // unique because the login has to find exactly one row for a username
    @Column(unique = true, nullable = false)
    private String username;

    // holds the BCrypt hash, never the plain text - about 60 characters
    @Column(nullable = false)
    private String password;

    private String role;   // "ADMIN" or "USER", checked by hasRole() in WebSecurityConfig

    public UserEntity() {}   // JPA needs the no-arg constructor to build rows by reflection

    public UserEntity(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // deliberately leaves the password out - this ends up in log output
    @Override
    public String toString() {
        return "UserEntity{id=" + id + ", username='" + username + "', role='" + role + "'}";
    }
}
