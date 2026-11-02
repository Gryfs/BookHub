package fr.eni.bookhub.service;

import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.domain.LoanStatus;
import fr.eni.bookhub.domain.Reservation;
import fr.eni.bookhub.domain.ReservationStatus;
import fr.eni.bookhub.domain.User;
import fr.eni.bookhub.dto.ReservationResponse;
import fr.eni.bookhub.repository.BookRepository;
import fr.eni.bookhub.repository.LoanRepository;
import fr.eni.bookhub.repository.ReservationRepository;
import fr.eni.bookhub.repository.UserRepository;
import fr.eni.bookhub.service.exception.BusinessRuleException;
import fr.eni.bookhub.service.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

  public static final int MAX_ACTIVE_RESERVATIONS = 3;
  public static final int PICKUP_HOURS = 48;

  private static final List<ReservationStatus> ACTIVE =
      List.of(ReservationStatus.WAITING, ReservationStatus.AVAILABLE);
  private static final List<LoanStatus> ONGOING_LOANS = List.of(LoanStatus.ACTIVE, LoanStatus.LATE);

  private final ReservationRepository reservationRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final LoanRepository loanRepository;
  private final EntityManager entityManager;

  @Transactional
  public ReservationResponse create(String email, Long bookId) {
    expireStaleReservations();

    User user = userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

    bookRepository.flush();
    entityManager.clear();
    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new ResourceNotFoundException("Livre", bookId));

    if (book.getAvailableCopies() > 0) {
      throw new BusinessRuleException("Ce livre est disponible : empruntez-le directement.");
    }

    if (loanRepository.existsByUserIdAndBookIdAndStatusIn(user.getId(), bookId, ONGOING_LOANS)) {
      throw new BusinessRuleException("Vous avez déjà emprunté ce livre.");
    }

    if (reservationRepository.existsByUserIdAndBookIdAndStatusIn(user.getId(), bookId, ACTIVE)) {
      throw new BusinessRuleException("Vous avez déjà une réservation active pour ce livre.");
    }

    if (reservationRepository.countByUserIdAndStatusIn(user.getId(), ACTIVE) >= MAX_ACTIVE_RESERVATIONS) {
      throw new BusinessRuleException("Vous ne pouvez pas avoir plus de 3 réservations actives.");
    }

    Reservation reservation = new Reservation();
    reservation.setUser(user);
    reservation.setBook(book);
    reservation.setStatus(ReservationStatus.WAITING);
    Reservation saved = reservationRepository.save(reservation);

    return toResponse(saved, waitingRanks());
  }

  @Transactional
  public List<ReservationResponse> listMine(String email) {
    expireStaleReservations();

    User user = userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

    Map<Long, Integer> ranks = waitingRanks();

    return reservationRepository
        .findByUserIdAndStatusInOrderByReservationDateAsc(user.getId(), ACTIVE).stream()
        .map(r -> ReservationResponse.from(r, ranks.get(r.getId())))
        .toList();
  }

  @Transactional
  public void cancel(String email, Long reservationId) {
    User user = userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

    Reservation reservation = reservationRepository.findByIdAndUserId(reservationId, user.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Réservation", reservationId));

    if (reservation.getStatus() == ReservationStatus.CANCELLED) {
      throw new BusinessRuleException("Cette réservation est déjà annulée.");
    }

    Long bookId = reservation.getBook().getId();
    boolean wasReady = reservation.getStatus() == ReservationStatus.AVAILABLE;
    reservation.setStatus(ReservationStatus.CANCELLED);
    reservation.setPickupDeadline(null);
    reservationRepository.save(reservation);

    if (wasReady) {
      onCopyAvailable(bookId);
    }
  }

  @Transactional
  public void onCopyAvailable(Long bookId) {
    bookRepository.flush();
    entityManager.clear();

    Book book = bookRepository.findById(bookId).orElse(null);
    if (book == null || book.getAvailableCopies() <= 0) {
      return;
    }

    if (reservationRepository.findFirstByBookIdAndStatusOrderByReservationDateAscIdAsc(bookId,
        ReservationStatus.AVAILABLE).isPresent()) {
      return;
    }

    reservationRepository.findFirstByBookIdAndStatusOrderByReservationDateAscIdAsc(bookId,
            ReservationStatus.WAITING)
        .ifPresent(next -> {
          next.setStatus(ReservationStatus.AVAILABLE);
          next.setPickupDeadline(OffsetDateTime.now().plusHours(PICKUP_HOURS));
          reservationRepository.save(next);
        });
  }

  @Transactional
  public int expireStaleReservations() {
    List<Reservation> expired = reservationRepository.findByStatusAndPickupDeadlineBefore(
        ReservationStatus.AVAILABLE, OffsetDateTime.now());

    for (Reservation reservation : expired) {
      Long bookId = reservation.getBook().getId();
      reservation.setStatus(ReservationStatus.CANCELLED);
      reservation.setPickupDeadline(null);
      reservationRepository.save(reservation);
      onCopyAvailable(bookId);
    }
    return expired.size();
  }

  @Transactional
  public void assertBorrowAllowed(User user, Book book) {
    Optional<Reservation> ready = reservationRepository
        .findFirstByBookIdAndStatusOrderByReservationDateAscIdAsc(book.getId(),
            ReservationStatus.AVAILABLE);

    if (ready.isPresent() && !ready.get().getUser().getId().equals(user.getId())) {
      throw new BusinessRuleException(
          "Un exemplaire est réservé pour un autre lecteur. Revenez plus tard.");
    }
  }

  @Transactional
  public void closeAvailableReservationOnBorrow(User user, Long bookId) {
    reservationRepository.findByUserIdAndStatusInOrderByReservationDateAsc(user.getId(), ACTIVE)
        .stream()
        .filter(r -> r.getBook().getId().equals(bookId)
            && r.getStatus() == ReservationStatus.AVAILABLE)
        .findFirst()
        .ifPresent(r -> {
          r.setStatus(ReservationStatus.CANCELLED);
          r.setPickupDeadline(null);
          reservationRepository.save(r);
        });
  }

  @Transactional(readOnly = true)
  public List<ReservationResponse> listReadyForPickup() {
    OffsetDateTime now = OffsetDateTime.now();
    return reservationRepository.findByStatusOrderByReservationDateAscIdAsc(ReservationStatus.AVAILABLE)
        .stream()
        .filter(r -> r.getPickupDeadline() != null && r.getPickupDeadline().isAfter(now))
        .map(r -> ReservationResponse.from(r, null))
        .toList();
  }

  private ReservationResponse toResponse(Reservation reservation, Map<Long, Integer> ranks) {
    Integer rank = reservation.getStatus() == ReservationStatus.WAITING
        ? ranks.get(reservation.getId())
        : null;
    return ReservationResponse.from(reservation, rank);
  }

  private Map<Long, Integer> waitingRanks() {
    Map<Long, Integer> ranks = new HashMap<>();
    Map<Long, Integer> perBook = new HashMap<>();

    reservationRepository.findByStatusOrderByReservationDateAscIdAsc(ReservationStatus.WAITING)
        .stream()
        .sorted(Comparator.comparing((Reservation r) -> r.getBook().getId())
            .thenComparing(Reservation::getReservationDate)
            .thenComparing(Reservation::getId))
        .forEach(r -> {
          long bookId = r.getBook().getId();
          int rank = perBook.getOrDefault(bookId, 0) + 1;
          perBook.put(bookId, rank);
          ranks.put(r.getId(), rank);
        });

    return ranks;
  }
}
