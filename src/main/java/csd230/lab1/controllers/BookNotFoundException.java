package csd230.lab1.controllers;

/**
 * Lecture 2.9: thrown when a REST caller asks for an id that is not in the table.
 * BookNotFoundAdvice is what turns it into a 404 response.
 */
public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Long id) {
        super("Could not find book with ID: " + id);
    }
}
