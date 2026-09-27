package csd230.lab1.repositories;

import com.github.javafaker.Faker;
import csd230.lab1.entities.BookEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // use the application db (mysql), not an embedded h2
@Transactional(propagation = Propagation.NOT_SUPPORTED)                      // don't roll back, so the rows stay visible in mysql
class BookEntityRepositoryTest {

    @Autowired
    private BookEntityRepository bookRepository;

    private final Faker faker = new Faker();

    private BookEntity newBook(String title, double price, String author, String isbn) {
        return new BookEntity(title, price, 10, author, isbn);
    }

    /** CRUD: create, read, update, delete one book. */
    @Test
    void crudOnBook() {
        String isbn = uniqueIsbn();

        // CREATE
        BookEntity saved = bookRepository.save(
                newBook(faker.book().title(), 42.50, faker.book().author(), isbn));
        Long id = saved.getId();
        assertTrue(id != null, "save() must assign a generated id");

        // READ
        BookEntity read = bookRepository.findById(id).orElseThrow();
        assertEquals(id, read.getId());
        assertEquals(isbn, read.getIsbn());
        assertEquals(42.50, read.getPrice(), 0.001);

        // UPDATE
        read.setPrice(9.99);
        read.setTitle("Updated Title");
        bookRepository.save(read);

        BookEntity updated = bookRepository.findById(id).orElseThrow();
        assertEquals(9.99, updated.getPrice(), 0.001);
        assertEquals("Updated Title", updated.getTitle());

        // DELETE
        bookRepository.deleteById(id);
        Optional<BookEntity> gone = bookRepository.findById(id);
        assertFalse(gone.isPresent(), "the book should be gone after deleteById");
    }

    /** Derived queries: findByIsbn, findByAuthor and a "Like" query. */
    @Test
    void derivedQueries() {
        String isbn = uniqueIsbn();
        String author = "Ada " + UUID.randomUUID().toString().substring(0, 8);

        bookRepository.save(newBook("The Girl Who Drew Dragons", 24.00, author, isbn));
        bookRepository.save(newBook("Dragons of Ordinary Farm", 31.00, author, uniqueIsbn()));

        // findByIsbn - the derived query the lab asks for
        List<BookEntity> byIsbn = bookRepository.findByIsbn(isbn);
        assertEquals(1, byIsbn.size());
        assertEquals("The Girl Who Drew Dragons", byIsbn.get(0).getTitle());

        // findByAuthor
        assertEquals(2, bookRepository.findByAuthor(author).size());

        // "Like" query - caller supplies the wildcards
        List<BookEntity> drew = bookRepository.findByTitleLike("%Drew%");
        assertTrue(drew.stream().anyMatch(b -> b.getTitle().equals("The Girl Who Drew Dragons")));

        // ContainingIgnoreCase wraps the argument in % itself
        assertEquals(2, bookRepository.findByAuthorContainingIgnoreCase(author.toLowerCase()).size());
    }

    /** Custom @Query (JPQL): price range and a case-insensitive keyword search. */
    @Test
    void customJpqlQueries() {
        String marker = UUID.randomUUID().toString().substring(0, 8);

        bookRepository.save(newBook("Cheap " + marker, 5.00, "Author A", uniqueIsbn()));
        bookRepository.save(newBook("MidRange " + marker, 55.00, "Author B", uniqueIsbn()));
        bookRepository.save(newBook("Pricey " + marker, 500.00, "Author C", uniqueIsbn()));

        List<BookEntity> inRange = bookRepository.findBooksInPriceRange(10.00, 100.00);
        assertTrue(inRange.stream().anyMatch(b -> b.getTitle().equals("MidRange " + marker)));
        assertTrue(inRange.stream().noneMatch(b -> b.getTitle().equals("Cheap " + marker)));
        assertTrue(inRange.stream().noneMatch(b -> b.getTitle().equals("Pricey " + marker)));
        assertTrue(inRange.stream().allMatch(b -> b.getPrice() >= 10.00 && b.getPrice() <= 100.00));

        List<BookEntity> keyword = bookRepository.searchByTitleKeyword("midrange " + marker);
        assertEquals(1, keyword.size());
        assertEquals("MidRange " + marker, keyword.get(0).getTitle());
    }

    /** Unique per test so the rows left behind by earlier runs never clash. */
    private static String uniqueIsbn() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 13);
    }
}
