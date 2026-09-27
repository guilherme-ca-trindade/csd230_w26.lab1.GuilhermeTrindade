package csd230.lab1.repositories;

import csd230.lab1.entities.MagazineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MagazineEntityRepository extends JpaRepository<MagazineEntity, Long> {

    List<MagazineEntity> findByTitle(String title);

    List<MagazineEntity> findByTitleLike(String pattern);

    List<MagazineEntity> findByOrderQtyGreaterThan(int qty);
}
