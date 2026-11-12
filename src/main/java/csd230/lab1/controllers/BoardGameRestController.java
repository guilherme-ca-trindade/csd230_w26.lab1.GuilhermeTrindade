package csd230.lab1.controllers;

import csd230.lab1.entities.BoardGameEntity;
import csd230.lab1.repositories.BoardGameEntityRepository;
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
 * Lab 5: JSON CRUD for the store's niche product.
 *
 * Same shape as the magazine, disc magazine and ticket controllers: plural noun in the path,
 * a 404 through RestExceptionAdvice when the id is unknown, and deletes routed through
 * ProductDeletionService so a game sitting in somebody's cart can still be removed.
 */
@Tag(name = "BoardGame REST API", description = "JSON CRUD for board games")
@RestController
@RequestMapping("/api/rest/boardgames")
public class BoardGameRestController {

    private final BoardGameEntityRepository repository;
    private final ProductDeletionService deletionService;

    public BoardGameRestController(BoardGameEntityRepository repository,
                                   ProductDeletionService deletionService) {
        this.repository = repository;
        this.deletionService = deletionService;
    }

    @Operation(summary = "List every board game",
            description = "Returns all board games as a JSON array")
    @ApiResponse(responseCode = "200", description = "Board games returned")
    @GetMapping
    public List<BoardGameEntity> all() {
        return repository.findAll();
    }

    @Operation(summary = "Get one board game by id")
    @ApiResponse(responseCode = "200", description = "Board game found")
    @ApiResponse(responseCode = "404", description = "No board game with that id")
    @GetMapping("/{id}")
    public BoardGameEntity one(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new BoardGameNotFoundException(id));
    }

    @Operation(summary = "Create a board game",
            description = "Saves the posted JSON as a new board game row")
    @ApiResponse(responseCode = "200", description = "Board game created, returned with its generated id")
    @PostMapping
    public BoardGameEntity newBoardGame(@RequestBody BoardGameEntity newBoardGame) {
        return repository.save(newBoardGame);
    }

    @Operation(summary = "Replace a board game", description = "Overwrites the board game at that id")
    @ApiResponse(responseCode = "200", description = "Board game replaced")
    @ApiResponse(responseCode = "404", description = "No board game with that id")
    @PutMapping("/{id}")
    public BoardGameEntity replaceBoardGame(@RequestBody BoardGameEntity newBoardGame,
                                            @PathVariable Long id) {
        return repository.findById(id)
                .map(boardGame -> {
                    boardGame.setTitle(newBoardGame.getTitle());
                    boardGame.setPrice(newBoardGame.getPrice());
                    boardGame.setCopies(newBoardGame.getCopies());
                    boardGame.setDesigner(newBoardGame.getDesigner());
                    boardGame.setMinPlayers(newBoardGame.getMinPlayers());
                    boardGame.setMaxPlayers(newBoardGame.getMaxPlayers());
                    boardGame.setPlayTimeMinutes(newBoardGame.getPlayTimeMinutes());
                    boardGame.setCooperative(newBoardGame.isCooperative());
                    boardGame.setReleaseDate(newBoardGame.getReleaseDate());
                    return repository.save(boardGame);
                })
                .orElseThrow(() -> new BoardGameNotFoundException(id));
    }

    @Operation(summary = "Delete a board game",
            description = "Takes the game out of any cart first. Refused if it already appears on an order.")
    @ApiResponse(responseCode = "200", description = "Board game deleted")
    @ApiResponse(responseCode = "404", description = "No board game with that id")
    @ApiResponse(responseCode = "409", description = "The board game has already been sold")
    @DeleteMapping("/{id}")
    public void deleteBoardGame(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new BoardGameNotFoundException(id);
        }
        deletionService.deleteProduct(id);
    }
}
