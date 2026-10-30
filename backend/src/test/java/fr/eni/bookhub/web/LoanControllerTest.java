package fr.eni.bookhub.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.domain.User;
import fr.eni.bookhub.repository.BookRepository;
import fr.eni.bookhub.repository.CategoryRepository;
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
class LoanControllerTest {

  private static final String PASSWORD = "BookHub!2026a";
  private static final String MARIE = "marie.lectrice@example.fr";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private PasswordEncoder passwordEncoder;

  private String createReader() {
    User reader = new User();
    reader.setEmail("lecteur.test@example.fr");
    reader.setPasswordHash(passwordEncoder.encode(PASSWORD));
    reader.setFirstName("Lecteur");
    reader.setLastName("Test");
    userRepository.save(reader);
    return reader.getEmail();
  }

  private Book createBook(String title, int copies) {
    Book book = new Book();
    book.setIsbn("978-0-00-" + (100000 + copies) + "-0");
    book.setTitle(title);
    book.setAuthor("Auteur Test");
    book.setTotalCopies(copies);
    book.setAvailableCopies(copies);
    book.setCategory(categoryRepository.findById(1).orElseThrow());
    return bookRepository.saveAndFlush(book);
  }

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

  @Test
  void borrowAvailableBook() throws Exception {
    Book book = createBook("Livre disponible", 2);

    mockMvc.perform(post("/api/loans")
            .header("Authorization", bearer(createReader()))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"bookId\": " + book.getId() + "}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.bookTitle").value("Livre disponible"))
        .andExpect(jsonPath("$.dueDate").exists())
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  void borrowUnavailableBookReturns409() throws Exception {
    Book book = createBook("Livre indisponible", 0);

    mockMvc.perform(post("/api/loans")
            .header("Authorization", bearer(createReader()))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"bookId\": " + book.getId() + "}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value(
            "Ce livre n'est pas disponible. Vous pouvez le réserver lorsque la réservation sera ouverte."));
  }

  @Test
  void borrowRequiresAuthentication() throws Exception {
    mockMvc.perform(post("/api/loans")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"bookId\": 9}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void myLoansListsCurrentAndHistory() throws Exception {
    mockMvc.perform(get("/api/loans/my").header("Authorization", bearer(MARIE)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.current").isArray())
        .andExpect(jsonPath("$.history").isArray())
        .andExpect(jsonPath("$.current[0].bookTitle").exists());
  }

}
