package fr.eni.bookhub.repository;

import fr.eni.bookhub.domain.Loan;
import fr.eni.bookhub.domain.LoanStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanRepository extends JpaRepository<Loan, Long> {

  @Override
  @EntityGraph(attributePaths = {"user", "book"})
  Page<Loan> findAll(Pageable pageable);

  @EntityGraph(attributePaths = {"user", "book"})
  Page<Loan> findByStatusIn(Collection<LoanStatus> statuses, Pageable pageable);

  @EntityGraph(attributePaths = "book")
  List<Loan> findByUserIdOrderByLoanDateDesc(Long userId);

  @EntityGraph(attributePaths = {"user", "book"})
  List<Loan> findByStatusInOrderByLoanDateAsc(Collection<LoanStatus> statuses);

  @Query("SELECT l FROM Loan l JOIN FETCH l.user JOIN FETCH l.book WHERE l.id = :id")
  Optional<Loan> findDetailedById(@Param("id") Long id);

  long countByUserIdAndStatusIn(Long userId, Collection<LoanStatus> statuses);

  boolean existsByUserIdAndStatus(Long userId, LoanStatus status);

  boolean existsByUserIdAndStatusIn(Long userId, Collection<LoanStatus> statuses);

  boolean existsByUserIdAndBookId(Long userId, Long bookId);

  boolean existsByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, Collection<LoanStatus> statuses);

  boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, LoanStatus status);

  long countByBookIdAndStatusIn(Long bookId, Collection<LoanStatus> statuses);

  boolean existsByBookId(Long bookId);

  @Query(value = "SELECT fn_marquer_retards()", nativeQuery = true)
  int markLateLoans();

}
