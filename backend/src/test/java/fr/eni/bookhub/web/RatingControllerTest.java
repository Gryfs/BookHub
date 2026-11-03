package fr.eni.bookhub.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.domain.Loan;
import fr.eni.bookhub.domain.LoanStatus;
import fr.eni.bookhub.repository.BookRepository;
import fr.eni.bookhub.repository.CategoryRepository;
import fr.eni.bookhub.repository.LoanRepository;
import fr.eni.bookhub.repository.RatingRepository;
import fr.eni.bookhub.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RatingControllerTest {

  private static final String PASSWORD = "BookHub!2026a";
  private static final String MARIE = "marie.lectrice@example.fr";
  private static final String LUCAS = "lucas.lecteur@example.fr";
  private static final String LIBRARIAN = "ahmed.biblio@quartier-solidaire.fr";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private LoanRepository loanRepository;

  @Autowired
  private RatingRepository ratingRepository;

  private String bearer(String email) throws Exception {
    String response = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .post("/api/auth/login")
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

  private Book bookWithoutRating() {
    Book book = new Book();
    book.setIsbn("978-2-97-" + System.nanoTime() % 1_000_000 + "-0");
    book.setTitle("Roman à noter");
    book.setAuthor("A. Test");
    book.setTotalCopies(2);
    book.setAvailableCopies(2);
    book.setCategory(categoryRepository.findById(1).orElseThrow());
    return bookRepository.saveAndFlush(book);
  }

  private void returnedLoan(String email, Book book) {
    Loan loan = new Loan();
    loan.setUser(userRepository.findByEmailIgnoreCase(email).orElseThrow());
    loan.setBook(book);
    loan.setLoanDate(LocalDate.now().minusDays(20));
    loan.setStatus(LoanStatus.RETURNED);
    loan.setReturnDate(LocalDate.now().minusDays(5));
    loanRepository.saveAndFlush(loan);
  }

  @Test
  void marieCanUpdateHerReviewOnFondation() throws Exception {
    mockMvc.perform(get("/api/ratings/books/3/context").header("Authorization", bearer(MARIE)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.canRate").value(true))
        .andExpect(jsonPath("$.myReview.score").value(5));

    mockMvc.perform(put("/api/ratings")
            .header("Authorization", bearer(MARIE))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"bookId": 3, "score": 4, "comment": "Toujours aussi solide en réédition."}
                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score").value(4))
        .andExpect(jsonPath("$.comment").value("Toujours aussi solide en réédition."));

    mockMvc.perform(get("/api/books/3"))
        .andExpect(jsonPath("$.reviews[?(@.authorLabel == 'MarieLit')].score").value(4));
  }

  @Test
  void cannotRateWithoutReturnedLoan() throws Exception {
    Book book = bookWithoutRating();

    mockMvc.perform(get("/api/ratings/books/" + book.getId() + "/context")
            .header("Authorization", bearer(LUCAS)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.canRate").value(false));

    mockMvc.perform(put("/api/ratings")
            .header("Authorization", bearer(LUCAS))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"bookId": %d, "score": 5, "comment": "Top"}
                """.formatted(book.getId())))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("Vous ne pouvez noter que les ouvrages que vous avez déjà empruntés puis rendus."));
  }

  @Test
  void lucasRatesAfterReturn() throws Exception {
    Book book = bookWithoutRating();
    returnedLoan(LUCAS, book);

    mockMvc.perform(put("/api/ratings")
            .header("Authorization", bearer(LUCAS))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"bookId": %d, "score": 3, "comment": "Correct sans plus."}
                """.formatted(book.getId())))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.score").value(3));

    assertThat(ratingRepository.countByBookId(book.getId())).isEqualTo(1);
  }

  @Test
  void librarianRemovesReview() throws Exception {
    long reviewId = ratingRepository.findByBookIdOrderByIdDesc(2L).getFirst().getId();
    long before = ratingRepository.countByBookId(2L);

    mockMvc.perform(delete("/api/librarian/reviews/" + reviewId)
            .header("Authorization", bearer(LIBRARIAN)))
        .andExpect(status().isNoContent());

    assertThat(ratingRepository.countByBookId(2L)).isEqualTo(before - 1);

    mockMvc.perform(get("/api/librarian/reviews").header("Authorization", bearer(LIBRARIAN)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.id == " + reviewId + ")]").isEmpty());
  }

  @Test
  void readerCannotAccessModerationList() throws Exception {
    mockMvc.perform(get("/api/librarian/reviews").header("Authorization", bearer(MARIE)))
        .andExpect(status().isForbidden());
  }

}
