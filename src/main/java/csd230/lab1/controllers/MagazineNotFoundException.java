package csd230.lab1.controllers;

public class MagazineNotFoundException extends ResourceNotFoundException {
    public MagazineNotFoundException(Long id) {
        super("magazine", id);
    }
}
