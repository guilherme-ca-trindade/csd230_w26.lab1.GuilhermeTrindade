package csd230.lab1.controllers;

import csd230.lab1.entities.TicketEntity;
import csd230.lab1.repositories.TicketEntityRepository;
import csd230.lab1.services.ProductDeletionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lab 4: JSON CRUD for tickets. A ticket is a ProductEntity but not a PublicationEntity, so it
 * has a description and a price, and no title or copies count.
 */
@Tag(name = "Ticket REST API", description = "JSON CRUD for event tickets")
@RestController
@RequestMapping("/api/rest/tickets")
public class TicketRestController {

    private final TicketEntityRepository repository;
    private final ProductDeletionService deletionService;

    public TicketRestController(TicketEntityRepository repository,
                                ProductDeletionService deletionService) {
        this.repository = repository;
        this.deletionService = deletionService;
    }

    @Operation(summary = "List every ticket")
    @ApiResponse(responseCode = "200", description = "Tickets returned")
    @GetMapping
    public List<TicketEntity> all() {
        return repository.findAll();
    }

    @Operation(summary = "Get one ticket by id")
    @ApiResponse(responseCode = "200", description = "Ticket found")
    @ApiResponse(responseCode = "404", description = "No ticket with that id")
    @GetMapping("/{id}")
    public TicketEntity one(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
    }

    @Operation(summary = "Create a ticket", description = "Saves the posted JSON as a new ticket row")
    @ApiResponse(responseCode = "200", description = "Ticket created, returned with its generated id")
    @PostMapping
    public TicketEntity newTicket(@RequestBody TicketEntity newTicket) {
        return repository.save(newTicket);
    }

    @Operation(summary = "Replace a ticket", description = "Overwrites the ticket at that id")
    @ApiResponse(responseCode = "200", description = "Ticket replaced")
    @ApiResponse(responseCode = "404", description = "No ticket with that id")
    @PutMapping("/{id}")
    public TicketEntity replaceTicket(@RequestBody TicketEntity newTicket, @PathVariable Long id) {
        return repository.findById(id)
                .map(ticket -> {
                    ticket.setDescription(newTicket.getDescription());
                    ticket.setPrice(newTicket.getPrice());
                    return repository.save(ticket);
                })
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    @Operation(summary = "Delete a ticket",
            description = "Takes it out of any cart first. Refused if it already appears on an order.")
    @ApiResponse(responseCode = "200", description = "Ticket deleted")
    @ApiResponse(responseCode = "404", description = "No ticket with that id")
    @ApiResponse(responseCode = "409", description = "The ticket has already been sold")
    @DeleteMapping("/{id}")
    public void deleteTicket(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new TicketNotFoundException(id);
        }
        deletionService.deleteProduct(id);
    }
}
