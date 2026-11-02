package csd230.lab1.controllers;

/**
 * Lab 4: base for the "no such row" exceptions the REST controllers throw. One base class
 * means RestExceptionAdvice needs a single handler instead of one per entity.
 *
 * BookNotFoundException is deliberately NOT part of this hierarchy - it is the Lecture 2.9
 * file and keeps its own advice, so the two never compete for the same exception.
 */
public abstract class ResourceNotFoundException extends RuntimeException {
    protected ResourceNotFoundException(String resource, Long id) {
        super("Could not find " + resource + " with ID: " + id);
    }
}
