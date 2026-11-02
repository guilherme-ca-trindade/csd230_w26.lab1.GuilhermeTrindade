package csd230.lab1.controllers;

public class DiscMagNotFoundException extends ResourceNotFoundException {
    public DiscMagNotFoundException(Long id) {
        super("disc magazine", id);
    }
}
