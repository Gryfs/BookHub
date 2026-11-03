package fr.eni.bookhub.service;

import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.domain.LoanStatus;
import fr.eni.bookhub.domain.Rating;
import fr.eni.bookhub.domain.User;
import fr.eni.bookhub.dto.BookReviewResponse;
import fr.eni.bookhub.dto.LibrarianReviewResponse;
import fr.eni.bookhub.dto.ReaderRatingContext;
import fr.eni.bookhub.dto.UpsertRatingRequest;
import fr.eni.bookhub.repository.BookRepository;
import fr.eni.bookhub.repository.LoanRepository;
import fr.eni.bookhub.repository.RatingRepository;
import fr.eni.bookhub.repository.UserRepository;
import fr.eni.bookhub.service.exception.BusinessRuleException;
import fr.eni.bookhub.service.exception.ResourceNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RatingService {

  private static final String NEED_RETURN =
      "Vous ne pouvez noter que les ouvrages que vous avez déjà empruntés puis rendus.";

  private final RatingRepository ratingRepository;
  private final BookRepository bookRepository;
  private final UserRepository userRepository;
  private final LoanRepository loanRepository;

  @Transactional(readOnly = true)
  public ReaderRatingContext contextFor(String email, Long bookId) {
    User user = userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

    bookRepository.findById(bookId)
        .orElseThrow(() -> new ResourceNotFoundException("Livre", bookId));

    BookReviewResponse mine = ratingRepository.findByUserIdAndBookId(user.getId(), bookId)
        .map(BookReviewResponse::from)
        .orElse(null);

    if (!mayRate(user.getId(), bookId)) {
      return ReaderRatingContext.blocked(NEED_RETURN);
    }
    return ReaderRatingContext.allowed(mine);
  }

  @Transactional
  public BookReviewResponse upsert(String email, UpsertRatingRequest request) {
    User user = userRepository.findByEmailIgnoreCase(email)
        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable"));

    Book book = bookRepository.findById(request.bookId())
        .orElseThrow(() -> new ResourceNotFoundException("Livre", request.bookId()));

    if (!mayRate(user.getId(), book.getId())) {
      throw new BusinessRuleException(NEED_RETURN);
    }

    Rating rating = ratingRepository.findByUserIdAndBookId(user.getId(), book.getId())
        .orElseGet(Rating::new);

    rating.setUser(user);
    rating.setBook(book);
    rating.setScore(request.score());
    rating.setComment(normalizeComment(request.comment()));

    return BookReviewResponse.from(ratingRepository.save(rating));
  }

  @Transactional(readOnly = true)
  public List<LibrarianReviewResponse> listRecentForModeration(int limit) {
    int capped = Math.min(Math.max(limit, 1), 100);
    return ratingRepository.findAllByOrderByIdDesc(PageRequest.of(0, capped)).stream()
        .map(LibrarianReviewResponse::from)
        .toList();
  }

  @Transactional
  public void removeByLibrarian(Long ratingId) {
    if (!ratingRepository.existsById(ratingId)) {
      throw new ResourceNotFoundException("Avis", ratingId);
    }
    ratingRepository.deleteById(ratingId);
  }

  private boolean mayRate(Long userId, Long bookId) {
    if (ratingRepository.existsByUserIdAndBookId(userId, bookId)) {
      return true;
    }
    return loanRepository.existsByUserIdAndBookIdAndStatus(userId, bookId, LoanStatus.RETURNED);
  }

  private static String normalizeComment(String comment) {
    if (comment == null) {
      return null;
    }
    String trimmed = comment.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

}
