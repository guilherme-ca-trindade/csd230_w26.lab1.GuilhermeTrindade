package csd230.lab1.controllers;

import csd230.lab1.entities.BookEntity;
import csd230.lab1.entities.CartEntity;
import csd230.lab1.entities.ProductEntity;
import csd230.lab1.repositories.BookEntityRepository;
import csd230.lab1.repositories.CartEntityRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Lecture 2.5.1: the shopping cart. There are no logins yet and nothing to tell one
 * visitor from another, so for now every request shares one hardcoded cart row.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    // stands in for "the logged in user's cart" until Spring Security - public so
    // OrderController and Application use this instead of repeating 1L
    public static final Long DEFAULT_CART_ID = 1L;

    private final CartEntityRepository cartRepository;
    private final BookEntityRepository bookRepository;

    public CartController(CartEntityRepository cartRepository,
                          BookEntityRepository bookRepository) {
        this.cartRepository = cartRepository;
        this.bookRepository = bookRepository;
    }

    @GetMapping
    public String viewCart(Model model) {
        CartEntity cart = findOrCreateDefaultCart();

        // worked out here, not in the template - a view displays numbers, it does not calculate them
        double total = cart.getProducts().stream()
                .mapToDouble(ProductEntity::getPrice)
                .sum();

        model.addAttribute("cart", cart);
        model.addAttribute("total", total);
        return "cartDetails";
    }

    /**
     * We save the CART and not the book: CartEntity is the side with the @JoinTable,
     * so saving it is what inserts the cart_products row.
     */
    @GetMapping("/add/{bookId}")
    public String addToCart(@PathVariable Long bookId, RedirectAttributes flash) {
        CartEntity cart = findOrCreateDefaultCart();
        BookEntity book = bookRepository.findById(bookId).orElse(null);

        if (book == null) {
            flash.addFlashAttribute("error", "That book could not be found.");
        } else if (cart.getProducts().contains(book)) {
            // a Set, so adding twice does nothing - better to say so than look broken
            flash.addFlashAttribute("error", "\"" + book.getTitle() + "\" is already in your cart.");
        } else {
            cart.addProduct(book);
            cartRepository.save(cart);
            flash.addFlashAttribute("message", "Added \"" + book.getTitle() + "\" to your cart.");
        }
        return "redirect:/books";
    }

    // deletes the join table row only - the product itself stays on sale
    @GetMapping("/remove/{bookId}")
    public String removeFromCart(@PathVariable Long bookId, RedirectAttributes flash) {
        CartEntity cart = findOrCreateDefaultCart();
        BookEntity book = bookRepository.findById(bookId).orElse(null);

        if (book != null) {
            cart.getProducts().remove(book);
            cartRepository.save(cart);
            flash.addFlashAttribute("message", "Removed \"" + book.getTitle() + "\" from your cart.");
        }
        return "redirect:/cart";
    }

    // findByIdWithProducts is the Lab 1 fetch join - the products come back with the cart
    private CartEntity findOrCreateDefaultCart() {
        return cartRepository.findByIdWithProducts(DEFAULT_CART_ID)
                .orElseGet(() -> cartRepository.save(new CartEntity()));
    }
}
