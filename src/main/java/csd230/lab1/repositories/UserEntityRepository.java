package csd230.lab1.repositories;

import csd230.lab1.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {

    // --- derived query: the lookup CustomUserDetailsService does on every login ---
    UserEntity findByUsername(String username);
}
