package csd230.lab1.repositories;

import csd230.lab1.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository over the whole SINGLE_TABLE hierarchy: findAll() returns books,
 * magazines, disc magazines and tickets together.
 */
@Repository
public interface ProductEntityRepository extends JpaRepository<ProductEntity, Long> {

    // --- custom JPQL: fetch join so the carts collection is ready outside a transaction ---
    @Query("SELECT p FROM ProductEntity p LEFT JOIN FETCH p.carts WHERE p.id = :id")
    Optional<ProductEntity> findByIdWithCarts(@Param("id") Long id);

    // --- custom JPQL: walks the many-to-many back from a product to its carts ---
    @Query("SELECT p FROM ProductEntity p JOIN p.carts c WHERE c.id = :cartId")
    List<ProductEntity> findAllInCart(@Param("cartId") Long cartId);

    // --- custom JPQL: how many carts a given product sits in ---
    @Query("SELECT COUNT(c) FROM ProductEntity p JOIN p.carts c WHERE p.id = :productId")
    long countCartsContaining(@Param("productId") Long productId);
}
