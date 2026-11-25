package csd230.lab1.controllers;

import csd230.lab1.entities.CartEntity;
import csd230.lab1.entities.UserEntity;
import csd230.lab1.repositories.CartEntityRepository;
import csd230.lab1.repositories.ProductEntityRepository;
import csd230.lab1.repositories.UserEntityRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

/**
 * Lecture 2.12: the JSON cart API.
 *
 * CartController is the separate MVC controller - its singular /cart actions answer with
 * HTML redirects and stay as they are. These plural /api/rest/carts actions answer with
 * JSON for the React frontend.
 *
 * The browser only ever sends a product id. The owner comes from Principal, so no request
 * can select somebody else's cart by sending a user id or a cart id.
 */
@Tag(name = "Cart REST API", description = "JSON cart for the signed-in user, plus the administrative list")
@RestController
@RequestMapping("/api/rest/carts")
public class CartRestController {

    private final CartEntityRepository repository;
    private final UserEntityRepository userRepository;
    private final ProductEntityRepository productRepository;

    public CartRestController(CartEntityRepository repository,
                              UserEntityRepository userRepository,
                              ProductEntityRepository productRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @PostMapping("/mine/products/{productId}")
    @Transactional
    @Operation(summary = "Add an existing product to the signed-in user's cart")
    @ApiResponse(responseCode = "200", description = "The saved cart")
    @ApiResponse(responseCode = "404", description = "No product with that id")
    public CartEntity addMine(@PathVariable Long productId, Principal principal) {
        var user = currentUser(principal, true);
        var product = productRepository.findById(productId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND));
        CartEntity cart = repository.findByUser(user);
        if (cart == null) {
            cart = new CartEntity();
            cart.setUser(user);
        }
        // products is a Set, so adding the same product twice keeps one membership
        cart.addProduct(product);
        return repository.save(cart);
    }

    // The authenticated username is the owner; no client cart/user ID is accepted.
    private UserEntity currentUser(Principal principal, boolean forUpdate) {
        var user = principal == null ? null : forUpdate
                ? userRepository.findForCartUpdate(principal.getName())
                : userRepository.findByUsername(principal.getName());
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED);
        }
        return user;
    }

    @GetMapping("/mine")
    @Transactional(readOnly = true)
    @Operation(summary = "Read the signed-in user's cart; 204 if none has been created")
    @ApiResponse(responseCode = "200", description = "The saved cart")
    @ApiResponse(responseCode = "204", description = "This user has no cart yet")
    public ResponseEntity<CartEntity> mine(Principal principal) {
        CartEntity cart = repository.findByUser(currentUser(principal, false));
        return cart == null ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(cart);
    }

    @DeleteMapping("/mine/products/{productId}")
    @Transactional
    @Operation(summary = "Remove a product from the signed-in user's cart")
    @ApiResponse(responseCode = "200", description = "The updated cart")
    @ApiResponse(responseCode = "204", description = "This user has no cart yet")
    @ApiResponse(responseCode = "404", description = "No product with that id")
    public ResponseEntity<CartEntity> removeMine(
            @PathVariable Long productId, Principal principal) {
        var user = currentUser(principal, true);
        var product = productRepository.findById(productId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND));
        CartEntity cart = repository.findByUser(user);
        if (cart == null) {
            return ResponseEntity.noContent().build();
        }
        // both sides of the many-to-many, so the join row actually goes away
        cart.getProducts().remove(product);
        product.getCarts().remove(cart);
        return ResponseEntity.ok(cart);
    }

    @GetMapping
    @Operation(summary = "List every cart", description = "Administrative view across all users")
    @ApiResponse(responseCode = "200", description = "Carts returned")
    public List<CartEntity> all() {
        return repository.findAllWithProducts();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one cart by id", description = "Administrative view")
    @ApiResponse(responseCode = "200", description = "Cart found")
    @ApiResponse(responseCode = "404", description = "No cart with that id")
    public CartEntity one(@PathVariable Long id) {
        return repository.findByIdWithProducts(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
