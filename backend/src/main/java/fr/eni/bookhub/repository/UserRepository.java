package fr.eni.bookhub.repository;

import fr.eni.bookhub.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmailIgnoreCase(String email);

  boolean existsByEmailIgnoreCase(String email);

  @Query(value = "SELECT 1 FROM fn_anonymiser_utilisateur(:userId)", nativeQuery = true)
  int anonymize(@Param("userId") Long userId);

}
