package csd230.lab1.controllers;

import csd230.lab1.entities.BookEntity;
import csd230.lab1.entities.CartEntity;
import csd230.lab1.entities.OrderEntity;
import csd230.lab1.repositories.BookEntityRepository;
import csd230.lab1.repositories.CartEntityRepository;
import csd230.lab1.repositories.OrderEntityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Lecture 2.5: the web front end for the books. @Controller means the methods return a
 * view name from src/main/resources/templates instead of JSON.
 *
 * Lecture 2.8: the @Tag / @Operation annotations are only there so SpringDoc can describe
 * these routes on the Swagger page. They change nothing at runtime, and the "schema" of
 * every response here is a rendered HTML page, which is exactly the point made in
 * Lecture 2.7 - compare this class with BookRestController.
 */
@Tag(name = "Books (MVC)", description = "Server rendered pages for the book catalogue - these return HTML, not JSON")
@Controller
@RequestMapping("/books")
public class BookController {

    private final BookEntityRepository bookRepository;
    private final CartEntityRepository cartRepository;
    private final OrderEntityRepository orderRepository;

    public BookController(BookEntityRepository bookRepository,
                          CartEntityRepository cartRepository,
                          OrderEntityRepository orderRepository) {
        this.bookRepository = bookRepository;
        this.cartRepository = cartRepository;
        this.orderRepository = orderRepository;
    }

    // typed to BookEntity, so Hibernate adds WHERE product_type = 'BOOK' by itself
    @Operation(summary = "Get all books", description = "Returns the HTML view of the book list")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list")
    @GetMapping
    public String getAllBooks(Model model) {
        model.addAttribute("books", bookRepository.findAll());
        return "bookList";
    }

    @Operation(summary = "Get one book", description = "Returns the HTML detail page for a single book")
    @ApiResponse(responseCode = "200", description = "Detail page rendered")
    @GetMapping("/{id}")
    public String getBookById(@PathVariable Long id, Model model) {
        BookEntity book = bookRepository.findById(id).orElse(null);
        model.addAttribute("book", book);
        return "bookDetails";
    }

    // the empty object is what th:object binds the form to
    @Operation(summary = "Show the add-book form", description = "ADMIN only")
    @GetMapping("/add")
    public String addBookForm(Model model) {
        model.addAttribute("book", new BookEntity());
        return "addBook";
    }

    /**
     * @ModelAttribute builds the BookEntity and calls the setters from the form fields.
     * Redirecting afterwards means a page refresh does not submit the form twice.
     */
    @Operation(summary = "Submit the add-book form", description = "Saves the book and redirects to /books. ADMIN only")
    @ApiResponse(responseCode = "302", description = "Redirect to /books")
    @PostMapping("/add")
    public String addBook(@ModelAttribute BookEntity book) {
        bookRepository.save(book);
        return "redirect:/books";
    }

    @Operation(summary = "Show the edit-book form", description = "ADMIN only")
    @GetMapping("/edit/{id}")
    public String editBookForm(@PathVariable Long id, Model model) {
        BookEntity book = bookRepository.findById(id).orElse(null);
        model.addAttribute("book", book);
        return "editBook";
    }

    /**
     * Copy the values onto the real row instead of saving the form object - the form has
     * no isbn or productId field, so saving it directly would wipe those columns.
     */
    @Operation(summary = "Submit the edit-book form", description = "Copies the submitted values onto the stored row. ADMIN only")
    @ApiResponse(responseCode = "302", description = "Redirect to /books")
    @PostMapping("/edit/{id}")
    public String editBook(@PathVariable Long id, @ModelAttribute BookEntity updatedBook) {
        BookEntity existingBook = bookRepository.findById(id).orElse(null);
        if (existingBook != null) {
            existingBook.setTitle(updatedBook.getTitle());
            existingBook.setAuthor(updatedBook.getAuthor());
            existingBook.setPrice(updatedBook.getPrice());
            existingBook.setCopies(updatedBook.getCopies());
            bookRepository.save(existingBook);
        }
        return "redirect:/books";
    }

    /**
     * Deleting a product still referenced by a join table throws
     * SQLIntegrityConstraintViolationException. A cart is just a shopping list so the book
     * is taken out of it first (Lecture 2.5), but an order is a receipt and must not
     * change, so a book that has already been sold is refused instead (Lab 2).
     */
    @Operation(summary = "Delete a book", description = "Refuses books that already appear on an order. ADMIN only")
    @ApiResponse(responseCode = "302", description = "Redirect to /books, with a flash message either way")
    @GetMapping("/delete/{id}")
    @Transactional
    public String deleteBook(@PathVariable Long id, RedirectAttributes flash) {
        BookEntity book = bookRepository.findById(id).orElse(null);
        if (book == null) {
            flash.addFlashAttribute("error", "That book no longer exists.");
            return "redirect:/books";
        }

        List<OrderEntity> ordersWithBook = orderRepository.findAllContainingProduct(id);
        if (!ordersWithBook.isEmpty()) {
            flash.addFlashAttribute("error", "Cannot delete \"" + book.getTitle()
                    + "\": it has already been sold on " + ordersWithBook.size() + " order(s).");
            return "redirect:/books";
        }

        // not cart.removeProduct(book) - that also edits book.getCarts(), the collection
        // this loop is walking, which would throw a ConcurrentModificationException
        for (CartEntity cart : book.getCarts()) {
            cart.getProducts().remove(book);
            cartRepository.save(cart);
        }
        bookRepository.deleteById(id);

        flash.addFlashAttribute("message", "Deleted \"" + book.getTitle() + "\".");
        return "redirect:/books";
    }
}
