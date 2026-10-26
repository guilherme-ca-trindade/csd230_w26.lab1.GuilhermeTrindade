package csd230.lab1;

import com.github.javafaker.Commerce;
import com.github.javafaker.Faker;
import csd230.lab1.entities.BookEntity;
import csd230.lab1.entities.CartEntity;
import csd230.lab1.entities.DiscMagEntity;
import csd230.lab1.entities.MagazineEntity;
import csd230.lab1.entities.ProductEntity;
import csd230.lab1.entities.TicketEntity;
import csd230.lab1.entities.UserEntity;
import csd230.lab1.repositories.BookEntityRepository;
import csd230.lab1.repositories.CartEntityRepository;
import csd230.lab1.repositories.DiscMagEntityRepository;
import csd230.lab1.repositories.MagazineEntityRepository;
import csd230.lab1.repositories.OrderEntityRepository;
import csd230.lab1.repositories.ProductEntityRepository;
import csd230.lab1.repositories.TicketEntityRepository;
import csd230.lab1.repositories.UserEntityRepository;
import jakarta.transaction.Transactional;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
public class Application implements CommandLineRunner {

    // constructor injection - Spring Data supplies the implementations at runtime
    private final ProductEntityRepository productRepository;
    private final CartEntityRepository cartRepository;
    private final BookEntityRepository bookRepository;
    private final MagazineEntityRepository magazineRepository;
    private final DiscMagEntityRepository discMagRepository;
    private final TicketEntityRepository ticketRepository;
    private final OrderEntityRepository orderRepository;   // Lab 2
    private final UserEntityRepository userRepository;     // Lecture 2.6
    private final PasswordEncoder passwordEncoder;         // Lecture 2.6, bean from WebSecurityConfig

    public Application(ProductEntityRepository productRepository,
                       CartEntityRepository cartRepository,
                       BookEntityRepository bookRepository,
                       MagazineEntityRepository magazineRepository,
                       DiscMagEntityRepository discMagRepository,
                       TicketEntityRepository ticketRepository,
                       OrderEntityRepository orderRepository,
                       UserEntityRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.bookRepository = bookRepository;
        this.magazineRepository = magazineRepository;
        this.discMagRepository = discMagRepository;
        this.ticketRepository = ticketRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // one CommandLineRunner only, as the lectures ask - it finishes during startup so
    // Tomcat can then serve requests, no console menu or input loop in here
    @Override
    @Transactional
    public void run(String... args) throws Exception {
        runLab1ConsoleDemo();   // Lab 1, unchanged
        seedWebStore();         // data the browser pages need
        seedUsers();            // Lecture 2.6, logins for the secured app
    }

    /** Lab 1: the repository CRUD walk-through. */
    private void runLab1ConsoleDemo() {
        Faker faker = new Faker();
        Commerce cm = faker.commerce();

        // ------------------------------------------------------------------
        // CREATE - seed the product hierarchy with JavaFaker data
        // ------------------------------------------------------------------
        banner("CREATE");

        com.github.javafaker.Book fakeBook = faker.book();
        BookEntity draftBook = new BookEntity(
                fakeBook.title(),
                Double.parseDouble(faker.commerce().price()),
                10,
                fakeBook.author(),
                faker.number().digits(13));

        BookEntity draftBook2 = new BookEntity(
                faker.book().title(),
                19.99,                       // fixed price so the price-range @Query has a hit
                5,
                faker.book().author(),
                "9780000000001");

        BookEntity draftBook3 = new BookEntity(
                "The Girl Who Drew Dragons",  // fixed title so the Like query has a hit
                149.50,                      // outside the price range below
                3,
                faker.book().author(),
                "9780000000002");

        MagazineEntity draftMagazine = new MagazineEntity(
                faker.lorem().word() + " Magazine",
                12.99,
                20,
                50,
                LocalDateTime.now());

        DiscMagEntity draftDiscMag = new DiscMagEntity(
                faker.lorem().word() + " DiscMag",
                24.99,
                8,
                30,
                LocalDateTime.now(),
                true);

        TicketEntity draftTicket = new TicketEntity(cm.productName() + " concert ticket", 89.95);
        TicketEntity draftTicket2 = new TicketEntity(cm.productName() + " cinema ticket", 14.50);

        // a human readable business key, separate from the generated primary key
        int sku = 1000;
        for (ProductEntity p : List.of(draftBook, draftBook2, draftBook3,
                draftMagazine, draftDiscMag, draftTicket, draftTicket2)) {
            p.setProductId("SKU-" + (sku++));
        }

        // Save the products FIRST so each one gets its generated id. Adding a still
        // transient product to a cart would make CascadeType.MERGE persist a *copy*
        // of it and leave the original without an id.
        final BookEntity book = bookRepository.save(draftBook);
        final BookEntity book2 = bookRepository.save(draftBook2);
        final BookEntity book3 = bookRepository.save(draftBook3);
        final MagazineEntity magazine = magazineRepository.save(draftMagazine);
        final DiscMagEntity discMag = discMagRepository.save(draftDiscMag);
        final TicketEntity ticket = ticketRepository.save(draftTicket);
        final TicketEntity ticket2 = ticketRepository.save(draftTicket2);

        final CartEntity cart1 = cartRepository.save(new CartEntity());
        cart1.addProduct(book);
        cart1.addProduct(magazine);
        cart1.addProduct(ticket);
        cartRepository.save(cart1);

        final CartEntity cart2 = cartRepository.save(new CartEntity());
        cart2.addProduct(book2);
        cart2.addProduct(discMag);
        // the SAME book row also goes in the second cart - that is the many-to-many
        cart2.addProduct(book);
        cartRepository.save(cart2);

        // book3 and ticket2 are saved above but belong to no cart

        System.out.println("Saved " + productRepository.count() + " products in "
                + cartRepository.count() + " carts.");

        // ------------------------------------------------------------------
        // READ - findAll / findById plus the Cart <-> Product relationship
        // ------------------------------------------------------------------
        banner("READ - all products");
        List<ProductEntity> allProducts = productRepository.findAll();
        allProducts.forEach(p -> System.out.println("  " + p));

        banner("READ - findById");
        Optional<ProductEntity> found = productRepository.findById(book.getId());
        found.ifPresent(p -> System.out.println("  found by id " + book.getId() + " -> " + p));

        banner("READ - carts and their products (many-to-many)");
        List<CartEntity> allCarts = cartRepository.findAll();
        for (CartEntity c : allCarts) {
            System.out.println(c.toString());
            for (ProductEntity p : c.getProducts()) {
                System.out.println("    " + p.toString());
            }
        }

        banner("READ - the other side: which carts hold this book?");
        System.out.println("  " + book + " is in " + book.getCarts().size() + " cart(s)");

        // ------------------------------------------------------------------
        // Derived queries (generated from the method names)
        // ------------------------------------------------------------------
        banner("DERIVED QUERIES");

        System.out.println("  findByIsbn(9780000000001):");
        bookRepository.findByIsbn("9780000000001").forEach(b -> System.out.println("    " + b));

        System.out.println("  findByTitle(" + magazine.getTitle() + "):");
        magazineRepository.findByTitle(magazine.getTitle()).forEach(m -> System.out.println("    " + m));

        System.out.println("  findByTitleLike(%Drew%):");
        bookRepository.findByTitleLike("%Drew%").forEach(b -> System.out.println("    " + b));

        System.out.println("  findByAuthorContainingIgnoreCase(" + book.getAuthor() + "):");
        bookRepository.findByAuthorContainingIgnoreCase(book.getAuthor())
                .forEach(b -> System.out.println("    " + b));

        System.out.println("  findByHasDisc(true):");
        discMagRepository.findByHasDisc(true).forEach(d -> System.out.println("    " + d));

        System.out.println("  findByDescriptionLike(%ticket%):");
        ticketRepository.findByDescriptionLike("%ticket%").forEach(t -> System.out.println("    " + t));

        // ------------------------------------------------------------------
        // Custom @Query (JPQL)
        // ------------------------------------------------------------------
        banner("CUSTOM @Query");

        System.out.println("  findBooksInPriceRange(10.00, 100.00):");
        bookRepository.findBooksInPriceRange(10.00, 100.00).forEach(b -> System.out.println("    " + b));

        System.out.println("  searchByTitleKeyword(dragons):");
        bookRepository.searchByTitleKeyword("dragons").forEach(b -> System.out.println("    " + b));

        System.out.println("  findAllInCart(" + cart1.getId() + "):");
        productRepository.findAllInCart(cart1.getId()).forEach(p -> System.out.println("    " + p));

        System.out.println("  countCartsContaining(book id " + book.getId() + ") = "
                + productRepository.countCartsContaining(book.getId()));

        // ------------------------------------------------------------------
        // UPDATE - change a managed entity and save it back
        // ------------------------------------------------------------------
        banner("UPDATE");
        BookEntity toUpdate = bookRepository.findById(book.getId()).orElseThrow();
        System.out.println("  before: " + toUpdate);
        toUpdate.setPrice(1.99);
        toUpdate.setTitle(toUpdate.getTitle() + " (2nd edition)");
        bookRepository.save(toUpdate);
        System.out.println("  after : " + bookRepository.findById(book.getId()).orElseThrow());

        // ------------------------------------------------------------------
        // DELETE - remove a product from a cart, then delete a product row
        // ------------------------------------------------------------------
        banner("DELETE");
        System.out.println("  cart " + cart1.getId() + " products before: " + cart1.getProducts().size());
        cart1.removeProduct(ticket);          // unlink only - breaks the join-table row
        cartRepository.save(cart1);
        System.out.println("  cart " + cart1.getId() + " products after : " + cart1.getProducts().size());

        long before = productRepository.count();
        ticketRepository.deleteById(ticket2.getId());   // ticket2 is in no cart, safe to delete
        productRepository.flush();
        System.out.println("  products " + before + " -> " + productRepository.count()
                + " after deleting ticket id " + ticket2.getId());

        banner("FINAL STATE");
        cartRepository.findAllWithProducts().forEach(c -> {
            System.out.println(c);
            c.getProducts().forEach(p -> System.out.println("    " + p));
        });
    }

    /**
     * Data for the web pages. The Lab 1 demo seeds random Faker books, which is fine for
     * testing queries but changes every run - these three have fixed titles and prices so
     * the checkout demo is repeatable.
     */
    private void seedWebStore() {
        // ------------------------------------------------------------------
        // WEB STORE SEED - fixed data for the browser pages
        // ------------------------------------------------------------------
        banner("WEB STORE SEED");

        // "The Hobbit" at 10 copies is the example the Lab 2 test flow uses
        addBookIfMissing("The Hobbit", 24.99, 10, "J.R.R. Tolkien", "9780261103344");
        addBookIfMissing("Spring MVC Basics", 29.99, 5, "Course Example", "9780000000011");
        addBookIfMissing("Thymeleaf in Practice", 34.99, 4, "Course Example", "9780000000012");

        // Lab 3: no shared cart is seeded any more. Carts belong to a user now, and
        // CartController creates one the first time that user opens their cart.
        // The carts the Lab 1 demo left behind have no owner and are unreachable
        // from the web pages.

        System.out.println("  books on sale   : " + bookRepository.count());
        System.out.println("  carts on file   : " + cartRepository.count() + " (Lab 1 demo, no owner)");
        System.out.println("  orders on file  : " + orderRepository.count());
    }

    /**
     * Lecture 2.6: the logins. Without this nobody could get past the login page, since
     * there is no registration form yet.
     */
    private void seedUsers() {
        // ------------------------------------------------------------------
        // USERS - the two accounts the security demo logs in with
        // ------------------------------------------------------------------
        banner("USERS");

        // encode() turns "admin" into a BCrypt hash before it ever reaches the database.
        // Writing the plain word here instead would make the login fail, because Spring
        // hashes what gets typed and compares hashes.
        addUserIfMissing("admin", "admin", "ADMIN");
        addUserIfMissing("user", "user", "USER");

        userRepository.findAll().forEach(u -> System.out.println("  " + u));
        System.out.println();
        System.out.println("  Ready - open http://localhost:8080/books");
        System.out.println("  Sign in with admin/admin (full access) or user/user (browse and buy)");
    }

    private void addUserIfMissing(String username, String rawPassword, String role) {
        if (userRepository.findByUsername(username) == null) {
            userRepository.save(new UserEntity(username, passwordEncoder.encode(rawPassword), role));
        }
    }

    private void addBookIfMissing(String title, double price, int copies, String author, String isbn) {
        if (bookRepository.findByTitle(title).isEmpty()) {
            BookEntity book = new BookEntity(title, price, copies, author, isbn);
            book.setProductId("SKU-" + isbn);
            bookRepository.save(book);
        }
    }

    private static void banner(String title) {
        System.out.println();
        System.out.println("================ " + title + " ================");
    }

    /**
     * Lecture 2.9: the JSON API is meant to be called from somewhere else - a React page
     * or a mobile app on another origin - and the browser blocks that by default. One
     * mapping for /api/** covers every REST controller, so none of them needs its own
     * @CrossOrigin annotation.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "DELETE");
            }
        };
    }
}
