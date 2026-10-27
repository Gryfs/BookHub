package fr.eni.bookhub.repository;

import static org.assertj.core.api.Assertions.assertThat;

import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.domain.Loan;
import fr.eni.bookhub.domain.LoanStatus;
import fr.eni.bookhub.domain.Reservation;
import fr.eni.bookhub.domain.ReservationStatus;
import fr.eni.bookhub.domain.Role;
import fr.eni.bookhub.domain.User;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private LoanRepository loanRepository;

  @Autowired
  private ReservationRepository reservationRepository;

  @Autowired
  private RatingRepository ratingRepository;

  @Test
  void findUserByEmailIgnoringCase() {
    User user = userRepository.findByEmailIgnoreCase("MARIE.LECTRICE@example.fr").orElseThrow();

    assertThat(user.getFirstName()).isEqualTo("Marie");
    assertThat(user.getRole()).isEqualTo(Role.USER);
  }

  @Test
  void searchBooksByTitleOrAuthor() {
    Page<Book> books = bookRepository
        .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase("harry", "harry", PageRequest.of(0, 10));

    assertThat(books.getContent())
        .extracting(Book::getTitle)
        .containsExactlyInAnyOrder("Harry Potter à l'école des sorciers",
            "La Vérité sur l'affaire Harry Quebert");
    assertThat(books.getContent().get(0).getCategory().getLabel()).isNotBlank();
  }

  @Test
  void availableCopiesAreUpdatedByTrigger() {
    Book dune = bookRepository.findById(4L).orElseThrow();

    assertThat(dune.getTotalCopies()).isEqualTo(1);
    assertThat(dune.getAvailableCopies()).isZero();
  }

  @Test
  void newBookStartsWithAllCopiesAvailable() {
    Book book = new Book();
    book.setIsbn("978-2-07-000000-0");
    book.setTitle("Livre de test");
    book.setAuthor("Auteur de test");
    book.setTotalCopies(3);
    book.setCategory(categoryRepository.findById(1).orElseThrow());

    Book saved = bookRepository.saveAndFlush(book);

    assertThat(saved.getId()).isNotNull();
    assertThat(saved.getAvailableCopies()).isEqualTo(3);
  }

  @Test
  void countActiveLoansOfUser() {
    long marie = loanRepository.countByUserIdAndStatusIn(3L, List.of(LoanStatus.ACTIVE, LoanStatus.LATE));

    assertThat(marie).isEqualTo(2);
    assertThat(loanRepository.existsByUserIdAndStatus(4L, LoanStatus.LATE)).isTrue();
    assertThat(loanRepository.existsByUserIdAndStatus(3L, LoanStatus.LATE)).isFalse();
  }

  @Test
  void loanDueDateIsFourteenDaysAfterLoanDate() {
    Loan loan = loanRepository.findById(1L).orElseThrow();

    assertThat(loan.getDueDate()).isEqualTo(loan.getLoanDate().plusDays(14));
  }

  @Test
  void markLateLoansDoesNothingWhenNoLoanIsOverdue() {
    assertThat(loanRepository.markLateLoans()).isZero();
  }

  @Test
  void reservationQueueIsOrderedByDate() {
    List<Reservation> queue = reservationRepository
        .findByBookIdAndStatusOrderByReservationDateAscIdAsc(4L, ReservationStatus.WAITING);

    assertThat(queue).extracting(r -> r.getUser().getId()).containsExactly(5L, 4L);
  }

  @Test
  void averageScoreOfBook() {
    assertThat(ratingRepository.findAverageScoreByBookId(2L)).isEqualTo(4.5);
    assertThat(ratingRepository.countByBookId(2L)).isEqualTo(2);
    assertThat(ratingRepository.findAverageScoreByBookId(1L)).isNull();
  }

}
