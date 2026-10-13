package csd230.lab1.controllers;

import csd230.lab1.entities.BookEntity;
import csd230.lab1.entities.CartEntity;
import csd230.lab1.entities.ProductEntity;
import csd230.lab1.entities.UserEntity;
import csd230.lab1.repositories.BookEntityRepository;
import csd230.lab1.repositories.CartEntityRepository;
import csd230.lab1.repositories.UserEntityRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/**
 * Lab 3: the cart now belongs to whoever is logged in. Lecture 2.5.1 shared one hardcoded
 * cart because there were no users yet - that constant is gone, and every lookup starts
 * from the Principal instead.
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartEntityRepository cartRepository;
    private final BookEntityRepository bookRepository;
    private final UserEntityRepository userRepository;

    public CartController(CartEntityRepository cartRepository,
                          BookEntityRepository bookRepository,
                          UserEntityRepository userRepository) {
        this.cartRepository = cartRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    /**
     * Lab 3: the one place a cart is resolved. Everything else goes through here, which is
     * what stops one user ever seeing another user's items.
     *
     * Principal is handed in by Spring Security - it is the logged in user. It can only be
     * null if the URL was reachable without a login, and WebSecurityConfig does not allow
     * that for /cart.
     */
    private CartEntity getCartForCurrentUser(Principal principal) {
        UserEntity user = userRepository.findByUsername(principal.getName());

        CartEntity cart = cartRepository.findByUser(user);

        // a brand new account has no cart yet - make one the first time they need it,
        // rather than creating carts for everyone at registration
        if (cart == null) {
            cart = new CartEntity();
            cart.setUser(user);
            cart = cartRepository.save(cart);
        }
        return cart;
    }

    @GetMapping
    public String viewCart(Model model, Principal principal) {
        CartEntity cart = getCartForCurrentUser(principal);

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
    public String addToCart(@PathVariable Long bookId, Principal principal, RedirectAttributes flash) {
        CartEntity cart = getCartForCurrentUser(principal);
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
    public String removeFromCart(@PathVariable Long bookId, Principal principal, RedirectAttributes flash) {
        CartEntity cart = getCartForCurrentUser(principal);
        BookEntity book = bookRepository.findById(bookId).orElse(null);

        if (book != null) {
            cart.getProducts().remove(book);
            cartRepository.save(cart);
            flash.addFlashAttribute("message", "Removed \"" + book.getTitle() + "\" from your cart.");
        }
        return "redirect:/cart";
    }
}
