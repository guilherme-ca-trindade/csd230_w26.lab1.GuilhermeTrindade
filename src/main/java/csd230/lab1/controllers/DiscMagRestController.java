package csd230.lab1.controllers;

import csd230.lab1.entities.DiscMagEntity;
import csd230.lab1.repositories.DiscMagEntityRepository;
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
 * Lab 4: JSON CRUD for disc magazines - magazines that ship with a cover disc. These rows also
 * appear under /api/rest/magazines, because DiscMagEntity extends MagazineEntity.
 */
@Tag(name = "DiscMag REST API", description = "JSON CRUD for magazines that ship with a disc")
@RestController
@RequestMapping("/api/rest/discmags")
public class DiscMagRestController {

    private final DiscMagEntityRepository repository;
    private final ProductDeletionService deletionService;

    public DiscMagRestController(DiscMagEntityRepository repository,
                                 ProductDeletionService deletionService) {
        this.repository = repository;
        this.deletionService = deletionService;
    }

    @Operation(summary = "List every disc magazine")
    @ApiResponse(responseCode = "200", description = "Disc magazines returned")
    @GetMapping
    public List<DiscMagEntity> all() {
        return repository.findAll();
    }

    @Operation(summary = "Get one disc magazine by id")
    @ApiResponse(responseCode = "200", description = "Disc magazine found")
    @ApiResponse(responseCode = "404", description = "No disc magazine with that id")
    @GetMapping("/{id}")
    public DiscMagEntity one(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new DiscMagNotFoundException(id));
    }

    @Operation(summary = "Create a disc magazine",
            description = "Saves the posted JSON as a new disc magazine row")
    @ApiResponse(responseCode = "200", description = "Disc magazine created, returned with its generated id")
    @PostMapping
    public DiscMagEntity newDiscMag(@RequestBody DiscMagEntity newDiscMag) {
        return repository.save(newDiscMag);
    }

    @Operation(summary = "Replace a disc magazine", description = "Overwrites the disc magazine at that id")
    @ApiResponse(responseCode = "200", description = "Disc magazine replaced")
    @ApiResponse(responseCode = "404", description = "No disc magazine with that id")
    @PutMapping("/{id}")
    public DiscMagEntity replaceDiscMag(@RequestBody DiscMagEntity newDiscMag, @PathVariable Long id) {
        return repository.findById(id)
                .map(discMag -> {
                    discMag.setTitle(newDiscMag.getTitle());
                    discMag.setPrice(newDiscMag.getPrice());
                    discMag.setCopies(newDiscMag.getCopies());
                    discMag.setOrderQty(newDiscMag.getOrderQty());
                    discMag.setCurrentIssue(newDiscMag.getCurrentIssue());
                    discMag.setHasDisc(newDiscMag.isHasDisc());
                    return repository.save(discMag);
                })
                .orElseThrow(() -> new DiscMagNotFoundException(id));
    }

    @Operation(summary = "Delete a disc magazine",
            description = "Takes it out of any cart first. Refused if it already appears on an order.")
    @ApiResponse(responseCode = "200", description = "Disc magazine deleted")
    @ApiResponse(responseCode = "404", description = "No disc magazine with that id")
    @ApiResponse(responseCode = "409", description = "The disc magazine has already been sold")
    @DeleteMapping("/{id}")
    public void deleteDiscMag(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new DiscMagNotFoundException(id);
        }
        deletionService.deleteProduct(id);
    }
}
