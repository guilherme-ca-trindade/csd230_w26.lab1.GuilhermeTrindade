package csd230.lab1.repositories;

import csd230.lab1.entities.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartEntityRepository extends JpaRepository<CartEntity, Long> {

    /**
     * Custom JPQL with a fetch join so the products come back with the cart in one
     * query - avoids a LazyInitializationException when printing outside a transaction.
     */
    @Query("SELECT DISTINCT c FROM CartEntity c LEFT JOIN FETCH c.products")
    List<CartEntity> findAllWithProducts();

    @Query("SELECT c FROM CartEntity c LEFT JOIN FETCH c.products WHERE c.id = :id")
    Optional<CartEntity> findByIdWithProducts(@Param("id") Long id);
}
