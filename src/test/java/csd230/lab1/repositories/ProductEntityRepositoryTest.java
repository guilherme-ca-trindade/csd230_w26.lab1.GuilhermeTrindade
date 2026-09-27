package csd230.lab1.repositories;

import csd230.lab1.entities.BookEntity;
import csd230.lab1.entities.DiscMagEntity;
import csd230.lab1.entities.ProductEntity;
import csd230.lab1.entities.TicketEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class ProductEntityRepositoryTest {

    @Autowired
    private ProductEntityRepository productRepository;

    @Autowired
    private TicketEntityRepository ticketRepository;

    @Autowired
    private DiscMagEntityRepository discMagRepository;

    /** SINGLE_TABLE inheritance: one findAll() over ProductEntity returns every subtype. */
    @Test
    void findAllReturnsTheWholeHierarchy() {
        String marker = UUID.randomUUID().toString().substring(0, 8);

        productRepository.save(new BookEntity("Hierarchy Book " + marker, 20.00, 1, "A. Writer", marker));
        productRepository.save(new TicketEntity("Hierarchy Ticket " + marker, 45.00));
        productRepository.save(new DiscMagEntity("Hierarchy DiscMag " + marker, 9.99, 2, 10,
                LocalDateTime.now(), true));

        List<ProductEntity> all = productRepository.findAll();
        assertTrue(all.stream().anyMatch(p -> p instanceof BookEntity));
        assertTrue(all.stream().anyMatch(p -> p instanceof TicketEntity));
        assertTrue(all.stream().anyMatch(p -> p instanceof DiscMagEntity));
    }

    /** Derived queries on the leaf repositories. */
    @Test
    void derivedQueriesOnLeafRepositories() {
        String marker = UUID.randomUUID().toString().substring(0, 8);

        ticketRepository.save(new TicketEntity("Concert " + marker, 75.00));
        discMagRepository.save(new DiscMagEntity("Coded " + marker, 11.50, 3, 12,
                LocalDateTime.now(), true));

        assertEquals(1, ticketRepository.findByDescription("Concert " + marker).size());
        assertEquals(1, ticketRepository.findByDescriptionLike("%" + marker + "%").size());
        assertTrue(ticketRepository.findByPriceLessThan(100.00).stream()
                .anyMatch(t -> ("Concert " + marker).equals(t.getDescription())));

        assertTrue(discMagRepository.findByHasDisc(true).stream()
                .anyMatch(d -> ("Coded " + marker).equals(d.getTitle())));
    }
}
