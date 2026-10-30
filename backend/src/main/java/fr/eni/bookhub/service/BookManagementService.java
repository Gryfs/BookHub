package fr.eni.bookhub.service;

import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.domain.Category;
import fr.eni.bookhub.domain.LoanStatus;
import fr.eni.bookhub.dto.BookAdminResponse;
import fr.eni.bookhub.dto.BookDetailResponse;
import fr.eni.bookhub.dto.BookRequest;
import fr.eni.bookhub.dto.PageResponse;
import fr.eni.bookhub.repository.BookRepository;
import fr.eni.bookhub.repository.BookSpecifications;
import fr.eni.bookhub.repository.CategoryRepository;
import fr.eni.bookhub.repository.LoanRepository;
import fr.eni.bookhub.repository.RatingRepository;
import fr.eni.bookhub.repository.ReservationRepository;
import fr.eni.bookhub.service.exception.BusinessRuleException;
import fr.eni.bookhub.service.exception.InvalidFieldException;
import fr.eni.bookhub.service.exception.ResourceNotFoundException;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookManagementService {

  private static final List<LoanStatus> ONGOING = List.of(LoanStatus.ACTIVE, LoanStatus.LATE);
  private static final String DUPLICATE_ISBN = "Cet ISBN est déjà utilisé par un autre livre du catalogue.";

  private final BookRepository bookRepository;
  private final CategoryRepository categoryRepository;
  private final LoanRepository loanRepository;
  private final ReservationRepository reservationRepository;
  private final RatingRepository ratingRepository;
  private final BookService bookService;
  private final EntityManager entityManager;

  @Transactional(readOnly = true)
  public PageResponse<BookAdminResponse> getBooks(String text, int page, int size) {
    return PageResponse.from(
        bookRepository.findAll(BookSpecifications.matchesText(text), PageRequest.of(page, size, Sort.by("title"))),
        BookAdminResponse::from);
  }

  @Transactional
  public BookDetailResponse createBook(BookRequest request) {
    if (bookRepository.existsByIsbnDigits(isbnDigits(request.isbn()))) {
      throw new BusinessRuleException(DUPLICATE_ISBN);
    }

    Book book = new Book();
    fillBook(book, request);
    book.setAvailableCopies(request.totalCopies());
    Book saved = bookRepository.save(book);

    return bookService.getBookDetail(saved.getId());
  }

  @Transactional
  public BookDetailResponse updateBook(long id, BookRequest request) {
    Book book = findBook(id);

    if (bookRepository.existsByIsbnDigitsAndIdNot(isbnDigits(request.isbn()), id)) {
      throw new BusinessRuleException(DUPLICATE_ISBN);
    }

    long borrowedCopies = loanRepository.countByBookIdAndStatusIn(id, ONGOING);
    if (request.totalCopies() < borrowedCopies) {
      throw new InvalidFieldException("totalCopies", "Il y a actuellement " + borrowedCopies
          + " exemplaire(s) emprunté(s) : le nombre d'exemplaires ne peut pas être inférieur.");
    }

    fillBook(book, request);
    bookRepository.flush();
    entityManager.refresh(book);

    return bookService.getBookDetail(id);
  }

  @Transactional
  public void deleteBook(long id) {
    Book book = findBook(id);

    if (loanRepository.countByBookIdAndStatusIn(id, ONGOING) > 0) {
      throw new BusinessRuleException("Impossible de supprimer « " + book.getTitle()
          + " » : un emprunt est en cours sur ce livre.");
    }
    if (loanRepository.existsByBookId(id)) {
      throw new BusinessRuleException("Impossible de supprimer « " + book.getTitle()
          + " » : ce livre a déjà été emprunté et son historique est conservé pour les statistiques.");
    }

    reservationRepository.deleteByBookId(id);
    ratingRepository.deleteByBookId(id);
    bookRepository.delete(book);
  }

  private Book findBook(long id) {
    return bookRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Livre", id));
  }

  private void fillBook(Book book, BookRequest request) {
    Category category = categoryRepository.findById(request.categoryId())
        .orElseThrow(() -> new InvalidFieldException("categoryId", "Cette catégorie n'existe pas."));

    book.setTitle(request.title().trim());
    book.setAuthor(request.author().trim());
    book.setIsbn(request.isbn().trim());
    book.setCategory(category);
    book.setDescription(emptyToNull(request.description()));
    book.setPublicationYear(request.publicationYear());
    book.setTotalCopies(request.totalCopies());
    book.setCoverUrl(emptyToNull(request.coverUrl()));
  }

  private String isbnDigits(String isbn) {
    return isbn.replace("-", "").trim();
  }

  private String emptyToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

}
