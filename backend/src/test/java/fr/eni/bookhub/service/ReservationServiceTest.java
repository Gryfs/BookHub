package fr.eni.bookhub.service;

import static org.assertj.core.api.Assertions.assertThat;

import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.domain.ReservationStatus;
import fr.eni.bookhub.repository.BookRepository;
import fr.eni.bookhub.repository.CategoryRepository;
import fr.eni.bookhub.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class ReservationServiceTest {

  private static final String INES = "ines.lectrice@example.fr";

  @Autowired
  private ReservationService reservationService;

  @Autowired
  private ReservationRepository reservationRepository;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Test
  void listMineReturnsWaitingWithQueuePosition() {
    var list = reservationService.listMine(INES);
    assertThat(list).isNotEmpty();
    assertThat(list.stream().anyMatch(r -> r.status() == ReservationStatus.WAITING
        && r.queuePosition() != null && r.queuePosition() >= 1)).isTrue();
  }

  @Test
  void createReservationOnUnavailableBook() {
    Book book = new Book();
    book.setIsbn("978-2-97-" + System.nanoTime() % 1_000_000);
    book.setTitle("Indispo résa");
    book.setAuthor("Auteur");
    book.setTotalCopies(1);
    book.setAvailableCopies(0);
    book.setCategory(categoryRepository.findById(1).orElseThrow());
    book = bookRepository.save(book);

    var created = reservationService.create("marie.lectrice@example.fr", book.getId());
    assertThat(created.status()).isEqualTo(ReservationStatus.WAITING);
    assertThat(reservationRepository.findById(created.id())).isPresent();
  }

}
