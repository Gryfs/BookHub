package fr.eni.bookhub.service;

import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.domain.Loan;
import fr.eni.bookhub.domain.LoanStatus;
import fr.eni.bookhub.domain.User;
import fr.eni.bookhub.dto.LoanResponse;
import fr.eni.bookhub.dto.MyLoansResponse;
import fr.eni.bookhub.repository.BookRepository;
import fr.eni.bookhub.repository.LoanRepository;
import fr.eni.bookhub.repository.UserRepository;
import fr.eni.bookhub.service.exception.BusinessRuleException;
import fr.eni.bookhub.service.exception.ResourceNotFoundException;
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

  @Transactional
  public LoanResponse borrow(String email, Long bookId) {
    loanRepository.markLateLoans();

    User user = userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

    if (loanRepository.existsByUserIdAndStatus(user.getId(), LoanStatus.LATE)) {
      throw new BusinessRuleException(
          "Vous avez un emprunt en retard. Rendez vos livres avant d'en emprunter un nouveau.");
    }

    long activeCount = loanRepository.countByUserIdAndStatusIn(user.getId(), ONGOING);
    if (activeCount >= MAX_ACTIVE_LOANS) {
      throw new BusinessRuleException("Vous avez déjà 3 emprunts en cours.");
    }

    if (loanRepository.existsByUserIdAndBookIdAndStatusIn(user.getId(), bookId, ONGOING)) {
      throw new BusinessRuleException("Vous avez déjà emprunté ce livre.");
    }

    Book book = bookRepository.findById(bookId)
        .orElseThrow(() -> new ResourceNotFoundException("Livre", bookId));

    if (book.getAvailableCopies() <= 0) {
      throw new BusinessRuleException(
          "Ce livre n'est pas disponible. Vous pouvez le réserver lorsque la réservation sera ouverte.");
    }

    Loan loan = new Loan();
    loan.setUser(user);
    loan.setBook(book);
    loan.setStatus(LoanStatus.ACTIVE);

    return LoanResponse.from(loanRepository.save(loan));
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

}
