package fr.eni.bookhub.service;

import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.domain.Loan;
import fr.eni.bookhub.domain.LoanStatus;
import fr.eni.bookhub.domain.User;
import fr.eni.bookhub.dto.LibrarianLoanResponse;
import fr.eni.bookhub.dto.LoanResponse;
import fr.eni.bookhub.dto.MyLoansResponse;
import fr.eni.bookhub.repository.BookRepository;
import fr.eni.bookhub.repository.LoanRepository;
import fr.eni.bookhub.repository.UserRepository;
import fr.eni.bookhub.service.exception.BusinessRuleException;
import fr.eni.bookhub.service.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoanService {

  public static final int MAX_ACTIVE_LOANS = 3;
  private static final List<LoanStatus> ONGOING = List.of(LoanStatus.ACTIVE, LoanStatus.LATE);

  private final LoanRepository loanRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final EntityManager entityManager;
  private final ReservationService reservationService;

  @Transactional
  public LoanResponse borrow(String email, Long bookId) {
    User user = userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    return openLoan(user, bookId, false);
  }

  @Transactional
  public LoanResponse borrowAtCounter(String userEmail, Long bookId) {
    User user = userRepository.findByEmailIgnoreCase(userEmail.trim())
        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));
    return openLoan(user, bookId, true);
  }

  @Transactional
  public List<LibrarianLoanResponse> listOutstandingLoans() {
    syncLateStatuses();
    return loanRepository.findByStatusInOrderByLoanDateAsc(ONGOING).stream()
        .sorted(Comparator.comparing(Loan::getDueDate))
        .map(LibrarianLoanResponse::from)
        .toList();
  }

  @Transactional
  public List<LibrarianLoanResponse> listOverdueLoans() {
    syncLateStatuses();
    return loanRepository.findByStatusInOrderByLoanDateAsc(List.of(LoanStatus.LATE)).stream()
        .map(LibrarianLoanResponse::from)
        .toList();
  }

  @Transactional
  public LibrarianLoanResponse registerReturn(Long loanId) {
    syncLateStatuses();

    Loan loan = loanRepository.findDetailedById(loanId)
        .orElseThrow(() -> new ResourceNotFoundException("Emprunt", loanId));

    if (loan.getStatus() == LoanStatus.RETURNED) {
      throw new BusinessRuleException("Cet emprunt est déjà clos.");
    }

    loan.setReturnDate(LocalDate.now());
    loan.setStatus(LoanStatus.RETURNED);
    Long bookId = loan.getBook().getId();
    Loan saved = loanRepository.save(loan);
    reservationService.onCopyAvailable(bookId);
    return LibrarianLoanResponse.from(saved);
  }

  @Transactional(readOnly = true)
  public MyLoansResponse getMyLoans(String email) {
    User user = userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

    List<LoanResponse> all = loanRepository.findByUserIdOrderByLoanDateDesc(user.getId()).stream()
        .map(LoanResponse::from)
        .toList();

    List<LoanResponse> current = all.stream()
        .filter(l -> l.status() == LoanStatus.ACTIVE || l.status() == LoanStatus.LATE)
        .toList();
    List<LoanResponse> history = all.stream()
        .filter(l -> l.status() == LoanStatus.RETURNED)
        .toList();

    return new MyLoansResponse(current, history);
  }

  private LoanResponse openLoan(User user, Long bookId, boolean atCounter) {
    syncLateStatuses();

    if (loanRepository.existsByUserIdAndStatus(user.getId(), LoanStatus.LATE)) {
      throw new BusinessRuleException(atCounter
          ? "Ce lecteur a un emprunt en retard. Les exemplaires doivent être rendus avant un nouvel emprunt."
          : "Vous avez un emprunt en retard. Rendez vos livres avant d'en emprunter un nouveau.");
    }

    long activeCount = loanRepository.countByUserIdAndStatusIn(user.getId(), ONGOING);
    if (activeCount >= MAX_ACTIVE_LOANS) {
      throw new BusinessRuleException(atCounter
          ? "Ce lecteur a déjà 3 emprunts en cours."
          : "Vous avez déjà 3 emprunts en cours.");
    }

    if (loanRepository.existsByUserIdAndBookIdAndStatusIn(user.getId(), bookId, ONGOING)) {
      throw new BusinessRuleException(atCounter
          ? "Ce lecteur a déjà emprunté ce livre."
          : "Vous avez déjà emprunté ce livre.");
    }

    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new ResourceNotFoundException("Livre", bookId));

    if (book.getAvailableCopies() <= 0) {
      throw new BusinessRuleException(
          "Ce livre n'est pas disponible. Vous pouvez le réserver lorsque la réservation sera ouverte.");
    }

    reservationService.assertBorrowAllowed(user, book);

    Loan loan = new Loan();
    loan.setUser(user);
    loan.setBook(book);
    loan.setStatus(LoanStatus.ACTIVE);

    Loan saved = loanRepository.save(loan);
    reservationService.closeAvailableReservationOnBorrow(user, bookId);
    return LoanResponse.from(saved);
  }

  private void syncLateStatuses() {
    loanRepository.markLateLoans();
    entityManager.flush();
    entityManager.clear();
  }

}
