package csd230.lab1.repositories;

import csd230.lab1.entities.TicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketEntityRepository extends JpaRepository<TicketEntity, Long> {

    List<TicketEntity> findByDescription(String description);

    List<TicketEntity> findByDescriptionLike(String pattern);

    List<TicketEntity> findByPriceLessThan(double price);
}
