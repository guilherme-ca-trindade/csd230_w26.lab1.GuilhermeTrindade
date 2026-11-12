package csd230.lab1.dtos.requests;

/**
 * Lecture 2.10: the JSON body of POST /api/rest/auth/login.
 *
 * A DTO (Data Transfer Object) carries API input or output. It is not a JPA entity - nothing
 * here is saved as a database row. The username is the one stored on UserEntity, not an email.
 */
public class LoginReq {

    private String username;
    private String password;

    public LoginReq(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
