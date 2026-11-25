package csd230.lab1.repositories;

import csd230.lab1.entities.UserEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {

    // --- derived query: the lookup CustomUserDetailsService does on every login ---
    UserEntity findByUsername(String username);

    // Lecture 2.12: serialize JSON cart mutations for one owner, including two requests
    // that both try to create that owner's first cart. The write lock lasts for the
    // transaction; ordinary authentication still uses findByUsername above.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where u.username = :username")
    UserEntity findForCartUpdate(@Param("username") String username);
}
