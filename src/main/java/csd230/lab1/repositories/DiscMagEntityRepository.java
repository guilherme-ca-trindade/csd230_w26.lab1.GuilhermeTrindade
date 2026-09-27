package csd230.lab1.repositories;

import csd230.lab1.entities.DiscMagEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiscMagEntityRepository extends JpaRepository<DiscMagEntity, Long> {

    List<DiscMagEntity> findByTitle(String title);

    List<DiscMagEntity> findByHasDisc(boolean hasDisc);

    List<DiscMagEntity> findByTitleLike(String pattern);
}
