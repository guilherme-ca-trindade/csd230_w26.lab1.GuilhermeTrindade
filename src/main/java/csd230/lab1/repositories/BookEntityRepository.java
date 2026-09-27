package csd230.lab1.repositories;

import csd230.lab1.entities.BookEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookEntityRepository extends JpaRepository<BookEntity, Long> {

    // --- derived queries: Spring Data builds the JPQL from the method name ---
    List<BookEntity> findByIsbn(String isbn);
    // JpaRepository already provides Optional<BookEntity> findById(Long id).

    List<BookEntity> findByTitle(String title);

    List<BookEntity> findByAuthor(String author);

    BookEntity findFirstByAuthor(String author);

    // "Like" query - the caller supplies the wildcards, e.g. findByTitleLike("%drew%")
    List<BookEntity> findByTitleLike(String pattern);

    // Containing wraps the argument in % itself and ignores case
    List<BookEntity> findByAuthorContainingIgnoreCase(String fragment);

    List<BookEntity> findByPriceBetween(double min, double max);

    // --- custom JPQL query: books inside a price range, cheapest first ---
    @Query("SELECT b FROM BookEntity b WHERE b.price >= :min AND b.price <= :max ORDER BY b.price ASC")
    List<BookEntity> findBooksInPriceRange(@Param("min") double min, @Param("max") double max);

    // --- custom JPQL query with a LIKE on a joined-in inherited field ---
    @Query("SELECT b FROM BookEntity b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<BookEntity> searchByTitleKeyword(@Param("keyword") String keyword);
}
