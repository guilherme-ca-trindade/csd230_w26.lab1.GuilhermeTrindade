package csd230.lab1.controllers;

import csd230.lab1.entities.BookEntity;
import csd230.lab1.repositories.BookEntityRepository;
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
 * Lecture 2.9: the same books as BookController, served as JSON instead of HTML.
 *
 * @RestController is @Controller + @ResponseBody, so a returned BookEntity is handed to an
 * HttpMessageConverter (Jackson) and written straight into the response body - there is no
 * view name and no Thymeleaf template involved. CORS for /api/** is configured once in
 * Application.corsConfigurer(), so this class needs no @CrossOrigin of its own.
 *
 * The method bodies are the lecture's code as written. Two of them have known failures
 * against this project's schema - see documentation/lectures2.7-2.9-rest-notes.md.
 */
@Tag(name = "Book REST API", description = "JSON API for managing books")
@RestController
@RequestMapping("/api/rest/books")
public class BookRestController {

    private final BookEntityRepository repository;

    public BookRestController(BookEntityRepository repository) {
        this.repository = repository;
    }

    @Operation(summary = "List every book", description = "Returns the whole book table as a JSON array")
    @ApiResponse(responseCode = "200", description = "Books returned")
    @GetMapping
    public List<BookEntity> all() {
        return repository.findAll();
    }

    /**
     * @RequestBody is the REST counterpart of @ModelAttribute: Jackson deserializes the JSON
     * body into a BookEntity, instead of Spring reading form fields off a POST.
     */
    @Operation(summary = "Create a book", description = "Saves the posted JSON as a new book row")
    @ApiResponse(responseCode = "200", description = "Book created; the saved row comes back with its generated id")
    @PostMapping
    public BookEntity newBook(@RequestBody BookEntity newBook) {
        return repository.save(newBook);
    }

    @Operation(summary = "Get one book by id")
    @ApiResponse(responseCode = "200", description = "Book found")
    @ApiResponse(responseCode = "404", description = "No book with that id")
    @GetMapping("/{id}")
    public BookEntity one(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    /**
     * PUT replaces the resource, which is what makes it idempotent: sending the same request
     * twice leaves the same single book behind, where POST would create two.
     *
     * Known failure: the orElseGet branch cannot work here. BookEntity ids come from
     * @GeneratedValue(IDENTITY), so save() on an object that already has an id is treated as a
     * merge, and merging a row that does not exist throws StaleObjectStateException - a 500,
     * not the 404 the lecture's section VI says to expect.
     */
    @Operation(summary = "Replace a book",
            description = "Overwrites the book at that id, or creates it at that id if it does not exist")
    @ApiResponse(responseCode = "200", description = "Book replaced")
    @PutMapping("/{id}")
    public BookEntity replaceBook(@RequestBody BookEntity newBook, @PathVariable Long id) {
        return repository.findById(id)
                .map(book -> {
                    book.setTitle(newBook.getTitle());
                    book.setAuthor(newBook.getAuthor());
                    book.setPrice(newBook.getPrice());
                    book.setCopies(newBook.getCopies());
                    return repository.save(book);
                })
                .orElseGet(() -> {
                    newBook.setId(id);
                    return repository.save(newBook);
                });
    }

    /**
     * Known failure: a book that is sitting in a cart cannot be deleted this way, because
     * cart_products still holds a row pointing at it. Hibernate raises a referential integrity
     * violation and the caller gets a 500. BookController.deleteBook handles the same case by
     * unlinking the carts first and refusing books that already appear on an order.
     */
    @Operation(summary = "Delete a book")
    @ApiResponse(responseCode = "200", description = "Book deleted")
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
