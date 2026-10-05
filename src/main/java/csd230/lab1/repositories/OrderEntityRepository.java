package csd230.lab1.repositories;

import csd230.lab1.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Lab 2: the completed orders. Extending JpaRepository is all that is required - save()
 * and findById() come for free.
 */
@Repository
public interface OrderEntityRepository extends JpaRepository<OrderEntity, Long> {

    // --- custom JPQL: fetch join so the products are loaded before the page renders ---
    @Query("SELECT o FROM OrderEntity o LEFT JOIN FETCH o.products WHERE o.id = :id")
    Optional<OrderEntity> findByIdWithProducts(@Param("id") Long id);

    // --- custom JPQL: which orders was this product sold on? ---
    // Order to Product is one directional, so there is no product.getOrders() to ask
    @Query("SELECT DISTINCT o FROM OrderEntity o JOIN o.products p WHERE p.id = :productId")
    List<OrderEntity> findAllContainingProduct(@Param("productId") Long productId);
}
