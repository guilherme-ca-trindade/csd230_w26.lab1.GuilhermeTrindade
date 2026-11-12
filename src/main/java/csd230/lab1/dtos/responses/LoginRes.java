package csd230.lab1.dtos.responses;

/**
 * Lecture 2.10: the body returned by a successful login - the username that was authenticated
 * and the signed token the client puts in its Authorization header from then on.
 */
public class LoginRes {

    private String username;
    private String token;

    public LoginRes(String username, String token) {
        this.username = username;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
