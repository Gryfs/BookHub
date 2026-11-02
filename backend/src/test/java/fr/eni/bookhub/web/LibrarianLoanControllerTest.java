package fr.eni.bookhub.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.domain.Loan;
import fr.eni.bookhub.domain.LoanStatus;
import fr.eni.bookhub.domain.User;
import fr.eni.bookhub.repository.BookRepository;
import fr.eni.bookhub.repository.CategoryRepository;
import fr.eni.bookhub.repository.LoanRepository;
import fr.eni.bookhub.repository.UserRepository;
import java.time.LocalDate;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LibrarianLoanControllerTest {

  private static final String PASSWORD = "BookHub!2026a";
  private static final String LIBRARIAN = "ahmed.biblio@quartier-solidaire.fr";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private LoanRepository loanRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  @Autowired
  private EntityManager entityManager;

  private String bearer(String email) throws Exception {
    String response = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email": "%s", "password": "%s"}
                """.formatted(email, PASSWORD)))
        .andExpect(status().isOk())
        .andReturn()
        .getResponse()
        .getContentAsString();
    return "Bearer " + JsonPath.read(response, "$.token");
  }

  private User createReader(String email) {
    User reader = new User();
    reader.setEmail(email);
    reader.setPasswordHash(passwordEncoder.encode(PASSWORD));
    reader.setFirstName("Camille");
    reader.setLastName("Rousseau");
    return userRepository.save(reader);
  }

  private Book createBook(String title, int copies) {
    Book book = new Book();
    book.setIsbn("978-2-00-" + System.nanoTime() % 1_000_000 + "-7");
    book.setTitle(title);
    book.setAuthor("Auteur Test");
    book.setTotalCopies(copies);
    book.setAvailableCopies(copies);
    book.setCategory(categoryRepository.findById(1).orElseThrow());
    return bookRepository.saveAndFlush(book);
  }

  private Loan borrowBook(User reader, Book book) {
    Loan loan = new Loan();
    loan.setUser(reader);
    loan.setBook(book);
    loan.setStatus(LoanStatus.ACTIVE);
    return loanRepository.saveAndFlush(loan);
  }

  @Test
  void readerCannotAccessLibrarianLoanRoutes() throws Exception {
    mockMvc.perform(get("/api/librarian/loans/outstanding")
            .header("Authorization", bearer("marie.lectrice@example.fr")))
        .andExpect(status().isForbidden());
  }

  @Test
  void registerReturnClosesLoanAndFreesCopy() throws Exception {
    User reader = createReader("retour.test@example.fr");
    Book book = createBook("Le retour du roi", 1);
    Loan loan = borrowBook(reader, book);
    bookRepository.flush();
    book = bookRepository.findById(book.getId()).orElseThrow();

    mockMvc.perform(post("/api/librarian/loans/{id}/return", loan.getId())
            .header("Authorization", bearer(LIBRARIAN)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("RETURNED"))
        .andExpect(jsonPath("$.returnDate").exists())
        .andExpect(jsonPath("$.userEmail").value("retour.test@example.fr"));

    bookRepository.flush();
    entityManager.clear();
    Book updated = bookRepository.findById(book.getId()).orElseThrow();
    org.assertj.core.api.Assertions.assertThat(updated.getAvailableCopies()).isEqualTo(1);
  }

  @Test
  void registerReturnTwiceReturns409() throws Exception {
    User reader = createReader("double.retour@example.fr");
    Book book = createBook("1984", 1);
    Loan loan = borrowBook(reader, book);

    mockMvc.perform(post("/api/librarian/loans/{id}/return", loan.getId())
            .header("Authorization", bearer(LIBRARIAN)))
        .andExpect(status().isOk());

    mockMvc.perform(post("/api/librarian/loans/{id}/return", loan.getId())
            .header("Authorization", bearer(LIBRARIAN)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("Cet emprunt est déjà clos."));
  }

  @Test
  void overdueListMarksAndReturnsLateLoans() throws Exception {
    User reader = createReader("retard.test@example.fr");
    Book book = createBook("Dune", 2);
    Loan loan = borrowBook(reader, book);
    loan.setLoanDate(LocalDate.now().minusDays(20));
    loanRepository.saveAndFlush(loan);

    mockMvc.perform(get("/api/librarian/loans/overdue")
            .header("Authorization", bearer(LIBRARIAN)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.bookTitle == 'Dune' && @.status == 'LATE')]").isNotEmpty());
  }

  @Test
  void counterLoanCreatesLoanForReader() throws Exception {
    User reader = createReader("comptoir.test@example.fr");
    Book book = createBook("Bel-Ami", 1);

    mockMvc.perform(post("/api/librarian/loans/counter")
            .header("Authorization", bearer(LIBRARIAN))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"userEmail": "%s", "bookId": %d}
                """.formatted(reader.getEmail(), book.getId())))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.bookTitle").value("Bel-Ami"))
        .andExpect(jsonPath("$.status").value("ACTIVE"));

    bookRepository.flush();
    entityManager.clear();
    Book updated = bookRepository.findById(book.getId()).orElseThrow();
    org.assertj.core.api.Assertions.assertThat(updated.getAvailableCopies()).isEqualTo(0);
  }

  @Test
  void outstandingListsOpenLoans() throws Exception {
    User reader = createReader("ouvert.test@example.fr");
    Book book = createBook("L'Étranger", 1);
    borrowBook(reader, book);

    mockMvc.perform(get("/api/librarian/loans/outstanding")
            .header("Authorization", bearer(LIBRARIAN)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.userEmail == 'ouvert.test@example.fr')]").isNotEmpty());
  }

}
