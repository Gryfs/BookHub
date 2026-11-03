package fr.eni.bookhub.repository;

import fr.eni.bookhub.domain.Rating;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RatingRepository extends JpaRepository<Rating, Long> {

  @EntityGraph(attributePaths = "user")
  List<Rating> findByBookIdOrderByIdDesc(Long bookId);

  @EntityGraph(attributePaths = "user")
  Optional<Rating> findByUserIdAndBookId(Long userId, Long bookId);

  @EntityGraph(attributePaths = {"user", "book"})
  List<Rating> findAllByOrderByIdDesc(Pageable pageable);

  boolean existsByUserIdAndBookId(Long userId, Long bookId);

  long countByBookId(Long bookId);

  void deleteByBookId(Long bookId);

  @Query("select avg(r.score) from Rating r where r.book.id = :bookId")
  Double findAverageScoreByBookId(@Param("bookId") Long bookId);

}
