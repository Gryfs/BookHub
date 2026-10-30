package fr.eni.bookhub.repository;

import fr.eni.bookhub.domain.Loan;
import fr.eni.bookhub.domain.LoanStatus;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LoanRepository extends JpaRepository<Loan, Long> {

  @Override
  @EntityGraph(attributePaths = {"user", "book"})
  Page<Loan> findAll(Pageable pageable);

  @EntityGraph(attributePaths = {"user", "book"})
  Page<Loan> findByStatusIn(Collection<LoanStatus> statuses, Pageable pageable);

  @EntityGraph(attributePaths = "book")
  List<Loan> findByUserIdOrderByLoanDateDesc(Long userId);

  long countByUserIdAndStatusIn(Long userId, Collection<LoanStatus> statuses);

  boolean existsByUserIdAndStatus(Long userId, LoanStatus status);

  boolean existsByUserIdAndStatusIn(Long userId, Collection<LoanStatus> statuses);

  boolean existsByUserIdAndBookId(Long userId, Long bookId);

  boolean existsByUserIdAndBookIdAndStatusIn(Long userId, Long bookId, Collection<LoanStatus> statuses);

  @Query(value = "SELECT fn_marquer_retards()", nativeQuery = true)
  int markLateLoans();

}
