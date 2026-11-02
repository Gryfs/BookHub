package fr.eni.bookhub.repository;

import fr.eni.bookhub.domain.Reservation;
import fr.eni.bookhub.domain.ReservationStatus;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

  @EntityGraph(attributePaths = "book")
  List<Reservation> findByUserIdAndStatusInOrderByReservationDateAsc(Long userId,
      Collection<ReservationStatus> statuses);

  @EntityGraph(attributePaths = {"user", "book"})
  Optional<Reservation> findByIdAndUserId(Long id, Long userId);

  @EntityGraph(attributePaths = "user")
  Optional<Reservation> findFirstByBookIdAndStatusOrderByReservationDateAscIdAsc(Long bookId,
      ReservationStatus status);

  @EntityGraph(attributePaths = "book")
  List<Reservation> findByStatusAndPickupDeadlineBefore(ReservationStatus status,
      OffsetDateTime deadline);

  long countByUserIdAndStatusIn(Long userId, Collection<ReservationStatus> statuses);

  boolean existsByUserIdAndBookIdAndStatusIn(Long userId, Long bookId,
      Collection<ReservationStatus> statuses);

  List<Reservation> findByBookIdAndStatusOrderByReservationDateAscIdAsc(Long bookId,
      ReservationStatus status);

  @EntityGraph(attributePaths = {"book", "user"})
  List<Reservation> findByStatusOrderByReservationDateAscIdAsc(ReservationStatus status);

  void deleteByBookId(Long bookId);

}
