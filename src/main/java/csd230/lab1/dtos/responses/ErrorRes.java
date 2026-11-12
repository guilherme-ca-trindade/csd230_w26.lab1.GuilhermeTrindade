package csd230.lab1.dtos.responses;

import org.springframework.http.HttpStatus;

/**
 * Lecture 2.10: the body AuthController returns when the credentials do not check out. The
 * message stays deliberately vague - "Invalid username or password" - so it does not reveal
 * which half was wrong.
 */
public class ErrorRes {

    HttpStatus httpStatus;
    String message;

    public ErrorRes(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
