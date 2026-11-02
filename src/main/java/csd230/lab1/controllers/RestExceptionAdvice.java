package csd230.lab1.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Lab 4: the global exception handler for the REST layer. Without it a missing row would come
 * back as Spring's Whitelabel error page with a 500, which tells a calling program nothing.
 *
 * Handling the ResourceNotFoundException base class covers magazines, disc magazines and
 * tickets in one method; BookNotFoundAdvice still covers the Lecture 2.9 book exception.
 */
@RestControllerAdvice
public class RestExceptionAdvice {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFoundHandler(ResourceNotFoundException ex) {
        return ex.getMessage();
    }

    // 409 and not 404: the row exists, the request is simply not allowed
    @ExceptionHandler(ProductInUseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String inUseHandler(ProductInUseException ex) {
        return ex.getMessage();
    }
}
