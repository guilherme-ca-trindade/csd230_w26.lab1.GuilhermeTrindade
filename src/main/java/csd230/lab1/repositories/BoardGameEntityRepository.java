package csd230.lab1.repositories;

import csd230.lab1.entities.BoardGameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardGameEntityRepository extends JpaRepository<BoardGameEntity, Long> {

    List<BoardGameEntity> findByTitle(String title);

    List<BoardGameEntity> findByDesigner(String designer);

    List<BoardGameEntity> findByCooperativeTrue();

    // a game is playable by a group of this size when its range covers the number
    List<BoardGameEntity> findByMinPlayersLessThanEqualAndMaxPlayersGreaterThanEqual(int players, int samePlayers);
}
