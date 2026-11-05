package fr.eni.bookhub.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.domain.User;
import fr.eni.bookhub.repository.BookRepository;
import fr.eni.bookhub.repository.CategoryRepository;
import fr.eni.bookhub.repository.UserRepository;
import fr.eni.bookhub.service.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class LoanServiceTest {

  private static final String PASSWORD = "BookHub!2026a";

  @Autowired
  private LoanService loanService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Test
  void blocksBorrowWhenReaderHasLateLoan() {
    assertThatThrownBy(() -> loanService.borrow("lucas.lecteur@example.fr", 2L))
        .isInstanceOf(BusinessRuleException.class)
        .hasMessageContaining("retard");
  }

  @Test
  void blocksFourthActiveLoan() {
    User reader = new User();
    reader.setEmail("quota.test." + System.nanoTime() + "@example.fr");
    reader.setPasswordHash(passwordEncoder.encode(PASSWORD));
    reader.setFirstName("Quota");
    reader.setLastName("Test");
    reader = userRepository.save(reader);
    final String readerEmail = reader.getEmail();

    Book book = new Book();
    book.setIsbn("978-2-99-" + System.nanoTime() % 1_000_000);
    book.setTitle("Exemplaire test");
    book.setAuthor("Auteur");
    book.setTotalCopies(5);
    book.setAvailableCopies(5);
    book.setCategory(categoryRepository.findById(1).orElseThrow());
    book = bookRepository.save(book);
    final Long fourthBookId = book.getId();

    for (int i = 0; i < 3; i++) {
      Book extra = new Book();
      extra.setIsbn("978-2-98-" + System.nanoTime() % 1_000_000 + i);
      extra.setTitle("Livre " + i);
      extra.setAuthor("Auteur");
      extra.setTotalCopies(2);
      extra.setAvailableCopies(2);
      extra.setCategory(categoryRepository.findById(1).orElseThrow());
      loanService.borrow(readerEmail, bookRepository.save(extra).getId());
    }

    assertThatThrownBy(() -> loanService.borrow(readerEmail, fourthBookId))
        .isInstanceOf(BusinessRuleException.class)
        .hasMessageContaining("3 emprunts");
  }

}
