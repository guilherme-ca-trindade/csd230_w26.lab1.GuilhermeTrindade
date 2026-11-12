package csd230.lab1.controllers;

public class BoardGameNotFoundException extends ResourceNotFoundException {
    public BoardGameNotFoundException(Long id) {
        super("board game", id);
    }
}
