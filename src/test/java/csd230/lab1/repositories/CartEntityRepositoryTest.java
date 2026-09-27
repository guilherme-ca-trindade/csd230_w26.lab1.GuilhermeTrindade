package csd230.lab1.repositories;

import com.github.javafaker.Faker;
import csd230.lab1.entities.BookEntity;
import csd230.lab1.entities.CartEntity;
import csd230.lab1.entities.MagazineEntity;
import csd230.lab1.entities.ProductEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class CartEntityRepositoryTest {

    @Autowired
    private CartEntityRepository cartRepository;

    @Autowired
    private ProductEntityRepository productRepository;

    private final Faker faker = new Faker();

    /** A cart persists its products through the cart_products join table. */
    @Test
    void cartPersistsItsProducts() {
        String marker = UUID.randomUUID().toString().substring(0, 8);

        CartEntity cart = cartRepository.save(new CartEntity());
        assertNotNull(cart.getId(), "the cart must get a generated id");

        // save the products first so they carry a generated id before they join a cart
        BookEntity book = productRepository.save(new BookEntity("Book " + marker, 30.00, 4,
                faker.book().author(), marker));
        MagazineEntity magazine = productRepository.save(
                new MagazineEntity("Magazine " + marker, 12.99, 20, 50, LocalDateTime.now()));

        cart.addProduct(book);
        cart.addProduct(magazine);
        CartEntity saved = cartRepository.save(cart);   // CascadeType.PERSIST/MERGE saves the products too

        // reload from the database with a fetch join so the collection is ready outside a transaction
        CartEntity reloaded = cartRepository.findByIdWithProducts(saved.getId()).orElseThrow();
        assertEquals(2, reloaded.getProducts().size(), "both products must come back with the cart");
        assertTrue(reloaded.getProducts().stream()
                .anyMatch(p -> p instanceof BookEntity b && ("Book " + marker).equals(b.getTitle())));
        assertTrue(reloaded.getProducts().stream()
                .anyMatch(p -> p instanceof MagazineEntity m && ("Magazine " + marker).equals(m.getTitle())));
    }

    /** The relationship really is many-to-many: one product can sit in two carts. */
    @Test
    void oneProductCanBelongToManyCarts() {
        String marker = UUID.randomUUID().toString().substring(0, 8);

        CartEntity cart1 = cartRepository.save(new CartEntity());
        BookEntity book = productRepository.save(new BookEntity("Shared " + marker, 21.00, 2,
                faker.book().author(), marker));
        cart1.addProduct(book);
        cart1 = cartRepository.save(cart1);

        Long bookId = book.getId();
        assertNotNull(bookId);
        assertEquals(1, productRepository.countCartsContaining(bookId));

        // put the very same product row into a second cart
        ProductEntity managedBook = productRepository.findByIdWithCarts(bookId).orElseThrow();
        CartEntity cart2 = cartRepository.save(new CartEntity());
        cart2.addProduct(managedBook);
        cart2 = cartRepository.save(cart2);

        assertEquals(2, productRepository.countCartsContaining(bookId),
                "the book must now be linked to two carts");

        // and both carts see it
        assertEquals(1, cartRepository.findByIdWithProducts(cart1.getId()).orElseThrow().getProducts().size());
        assertEquals(1, cartRepository.findByIdWithProducts(cart2.getId()).orElseThrow().getProducts().size());

        // the custom JPQL that walks the join table from the cart side
        assertEquals(1, productRepository.findAllInCart(cart2.getId()).size());
        assertEquals(bookId, productRepository.findAllInCart(cart2.getId()).get(0).getId());
    }

    /** Removing a product from a cart deletes the join row but keeps the product. */
    @Test
    void removingAProductOnlyBreaksTheLink() {
        String marker = UUID.randomUUID().toString().substring(0, 8);

        CartEntity cart = cartRepository.save(new CartEntity());
        BookEntity book = productRepository.save(new BookEntity("Removable " + marker, 15.00, 1,
                faker.book().author(), marker));
        cart.addProduct(book);
        cart = cartRepository.save(cart);

        Long bookId = book.getId();
        assertEquals(1, cartRepository.findByIdWithProducts(cart.getId()).orElseThrow().getProducts().size());

        cart.getProducts().clear();
        cart = cartRepository.save(cart);

        assertEquals(0, cartRepository.findByIdWithProducts(cart.getId()).orElseThrow().getProducts().size());
        assertTrue(productRepository.findById(bookId).isPresent(), "the product row itself must survive");
        assertEquals(0, productRepository.countCartsContaining(bookId));
    }
}
