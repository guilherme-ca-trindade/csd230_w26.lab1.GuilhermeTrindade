package csd230.lab1.controllers;

public class TicketNotFoundException extends ResourceNotFoundException {
    public TicketNotFoundException(Long id) {
        super("ticket", id);
    }
}
