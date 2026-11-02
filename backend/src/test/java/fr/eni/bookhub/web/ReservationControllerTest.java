package fr.eni.bookhub.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.domain.ReservationStatus;
import fr.eni.bookhub.domain.User;
import fr.eni.bookhub.repository.BookRepository;
import fr.eni.bookhub.repository.CategoryRepository;
import fr.eni.bookhub.repository.ReservationRepository;
import fr.eni.bookhub.repository.UserRepository;
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
class ReservationControllerTest {

  private static final String PASSWORD = "BookHub!2026a";
  private static final String MARIE = "marie.lectrice@example.fr";
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
  private ReservationRepository reservationRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

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

  private Book unavailableBook() {
    Book book = new Book();
    book.setIsbn("978-2-99-" + System.nanoTime() % 1_000_000 + "-1");
    book.setTitle("Ouvrage indisponible");
    book.setAuthor("Auteur");
    book.setTotalCopies(1);
    book.setAvailableCopies(0);
    book.setCategory(categoryRepository.findById(1).orElseThrow());
    return bookRepository.saveAndFlush(book);
  }

  @Test
  void reserveUnavailableBook() throws Exception {
    Book book = unavailableBook();

    mockMvc.perform(post("/api/reservations")
            .header("Authorization", bearer(MARIE))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"bookId\": " + book.getId() + "}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("WAITING"))
        .andExpect(jsonPath("$.queuePosition").value(1));
  }

  @Test
  void cannotReserveAvailableBook() throws Exception {
    Book book = new Book();
    book.setIsbn("978-2-98-" + System.nanoTime() % 1_000_000 + "-2");
    book.setTitle("Dispo");
    book.setAuthor("A");
    book.setTotalCopies(2);
    book.setAvailableCopies(2);
    book.setCategory(categoryRepository.findById(1).orElseThrow());
    book = bookRepository.saveAndFlush(book);

    mockMvc.perform(post("/api/reservations")
            .header("Authorization", bearer(MARIE))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"bookId\": " + book.getId() + "}"))
        .andExpect(status().isConflict());
  }

  @Test
  void myReservationsListsActiveOnes() throws Exception {
    mockMvc.perform(get("/api/reservations/my").header("Authorization", bearer(MARIE)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray());
  }

  @Test
  void returnPromotesFirstInQueue() throws Exception {
    User holder = new User();
    holder.setEmail("detenteur.test@example.fr");
    holder.setPasswordHash(passwordEncoder.encode(PASSWORD));
    holder.setFirstName("Det");
    holder.setLastName("Test");
    userRepository.save(holder);

    User waiter = new User();
    waiter.setEmail("file.test@example.fr");
    waiter.setPasswordHash(passwordEncoder.encode(PASSWORD));
    waiter.setFirstName("File");
    waiter.setLastName("Test");
    userRepository.save(waiter);

    Book book = new Book();
    book.setIsbn("978-2-97-" + System.nanoTime() % 1_000_000 + "-3");
    book.setTitle("File unique");
    book.setAuthor("A");
    book.setTotalCopies(1);
    book.setAvailableCopies(1);
    book.setCategory(categoryRepository.findById(1).orElseThrow());
    book = bookRepository.saveAndFlush(book);

    String loanJson = mockMvc.perform(post("/api/librarian/loans/counter")
            .header("Authorization", bearer(LIBRARIAN))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"userEmail": "detenteur.test@example.fr", "bookId": %d}
                """.formatted(book.getId())))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    long loanId = ((Number) JsonPath.read(loanJson, "$.id")).longValue();

    mockMvc.perform(post("/api/reservations")
            .header("Authorization", bearer("file.test@example.fr"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"bookId\": " + book.getId() + "}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("WAITING"));

    mockMvc.perform(post("/api/librarian/loans/{id}/return", loanId)
            .header("Authorization", bearer(LIBRARIAN)))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/reservations/my").header("Authorization", bearer("file.test@example.fr")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[?(@.status == 'AVAILABLE')]").isNotEmpty())
        .andExpect(jsonPath("$[?(@.pickupDeadline != null)]").isNotEmpty());
  }

  @Test
  void cancelReservation() throws Exception {
    Book book = unavailableBook();
    String body = mockMvc.perform(post("/api/reservations")
            .header("Authorization", bearer(MARIE))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"bookId\": " + book.getId() + "}"))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    long id = ((Number) JsonPath.read(body, "$.id")).longValue();

    mockMvc.perform(delete("/api/reservations/{id}", id).header("Authorization", bearer(MARIE)))
        .andExpect(status().isNoContent());
  }

}
