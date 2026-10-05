package csd230.lab1.controllers;

import csd230.lab1.entities.BookEntity;
import csd230.lab1.entities.CartEntity;
import csd230.lab1.entities.OrderEntity;
import csd230.lab1.repositories.BookEntityRepository;
import csd230.lab1.repositories.CartEntityRepository;
import csd230.lab1.repositories.OrderEntityRepository;
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
 */
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
    @GetMapping
    public String getAllBooks(Model model) {
        model.addAttribute("books", bookRepository.findAll());
        return "bookList";
    }

    @GetMapping("/{id}")
    public String getBookById(@PathVariable Long id, Model model) {
        BookEntity book = bookRepository.findById(id).orElse(null);
        model.addAttribute("book", book);
        return "bookDetails";
    }

    // the empty object is what th:object binds the form to
    @GetMapping("/add")
    public String addBookForm(Model model) {
        model.addAttribute("book", new BookEntity());
        return "addBook";
    }

    /**
     * @ModelAttribute builds the BookEntity and calls the setters from the form fields.
     * Redirecting afterwards means a page refresh does not submit the form twice.
     */
    @PostMapping("/add")
    public String addBook(@ModelAttribute BookEntity book) {
        bookRepository.save(book);
        return "redirect:/books";
    }

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
