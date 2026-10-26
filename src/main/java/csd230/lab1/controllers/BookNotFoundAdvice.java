package csd230.lab1.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Lecture 2.9: the MVC side answers a bad id with a redirect and a flash message. An API has
 * to answer with a status code instead, because that is all the calling program reads.
 * @RestControllerAdvice applies this to every REST controller, so none of them has to catch
 * the exception itself.
 */
@RestControllerAdvice
class BookNotFoundAdvice {

    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String bookNotFoundHandler(BookNotFoundException ex) {
        return ex.getMessage();
    }
}
