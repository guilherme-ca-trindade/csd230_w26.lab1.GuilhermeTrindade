package csd230.lab1.controllers;

import csd230.lab1.entities.MagazineEntity;
import csd230.lab1.repositories.MagazineEntityRepository;
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
 * Lab 4: JSON CRUD for magazines.
 *
 * MagazineEntityRepository is typed to MagazineEntity and DiscMagEntity extends it, so this
 * list includes disc magazines - a disc magazine is a magazine. /api/rest/discmags is the
 * narrower view over the same rows.
 */
@Tag(name = "Magazine REST API", description = "JSON CRUD for magazines, including disc magazines")
@RestController
@RequestMapping("/api/rest/magazines")
public class MagazineRestController {

    private final MagazineEntityRepository repository;
    private final ProductDeletionService deletionService;

    public MagazineRestController(MagazineEntityRepository repository,
                                  ProductDeletionService deletionService) {
        this.repository = repository;
        this.deletionService = deletionService;
    }

    @Operation(summary = "List every magazine",
            description = "Returns all magazines and disc magazines as a JSON array")
    @ApiResponse(responseCode = "200", description = "Magazines returned")
    @GetMapping
    public List<MagazineEntity> all() {
        return repository.findAll();
    }

    @Operation(summary = "Get one magazine by id")
    @ApiResponse(responseCode = "200", description = "Magazine found")
    @ApiResponse(responseCode = "404", description = "No magazine with that id")
    @GetMapping("/{id}")
    public MagazineEntity one(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new MagazineNotFoundException(id));
    }

    @Operation(summary = "Create a magazine", description = "Saves the posted JSON as a new magazine row")
    @ApiResponse(responseCode = "200", description = "Magazine created, returned with its generated id")
    @PostMapping
    public MagazineEntity newMagazine(@RequestBody MagazineEntity newMagazine) {
        return repository.save(newMagazine);
    }

    @Operation(summary = "Replace a magazine", description = "Overwrites the magazine at that id")
    @ApiResponse(responseCode = "200", description = "Magazine replaced")
    @ApiResponse(responseCode = "404", description = "No magazine with that id")
    @PutMapping("/{id}")
    public MagazineEntity replaceMagazine(@RequestBody MagazineEntity newMagazine, @PathVariable Long id) {
        return repository.findById(id)
                .map(magazine -> {
                    magazine.setTitle(newMagazine.getTitle());
                    magazine.setPrice(newMagazine.getPrice());
                    magazine.setCopies(newMagazine.getCopies());
                    magazine.setOrderQty(newMagazine.getOrderQty());
                    magazine.setCurrentIssue(newMagazine.getCurrentIssue());
                    return repository.save(magazine);
                })
                .orElseThrow(() -> new MagazineNotFoundException(id));
    }

    @Operation(summary = "Delete a magazine",
            description = "Takes the magazine out of any cart first. Refused if it already appears on an order.")
    @ApiResponse(responseCode = "200", description = "Magazine deleted")
    @ApiResponse(responseCode = "404", description = "No magazine with that id")
    @ApiResponse(responseCode = "409", description = "The magazine has already been sold")
    @DeleteMapping("/{id}")
    public void deleteMagazine(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new MagazineNotFoundException(id);
        }
        deletionService.deleteProduct(id);
    }
}
