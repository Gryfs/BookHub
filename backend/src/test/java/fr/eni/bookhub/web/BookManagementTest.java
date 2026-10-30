package fr.eni.bookhub.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.domain.Loan;
import fr.eni.bookhub.domain.LoanStatus;
import fr.eni.bookhub.repository.BookRepository;
import fr.eni.bookhub.repository.CategoryRepository;
import fr.eni.bookhub.repository.LoanRepository;
import fr.eni.bookhub.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookManagementTest {

  private static final String PASSWORD = "BookHub!2026a";
  private static final String USER = "marie.lectrice@example.fr";
  private static final String LIBRARIAN = "ahmed.biblio@quartier-solidaire.fr";
  private static final String ADMIN = "sophie.admin@quartier-solidaire.fr";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private BookRepository bookRepository;

  @Autowired
  private CategoryRepository categoryRepository;

  @Autowired
  private LoanRepository loanRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManager entityManager;

  private String bearer(String email) throws Exception {
    String response = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"email": "%s", "password": "%s"}
                """.formatted(email, PASSWORD)))
        .andReturn().getResponse().getContentAsString();
    return "Bearer " + JsonPath.read(response, "$.token");
  }

  private String bookJson(String isbn, int totalCopies) {
    return """
        {"title": "Germinal", "author": "Émile Zola", "isbn": "%s", "categoryId": 1,
         "description": "La vie des mineurs.", "publicationYear": 1885, "totalCopies": %d,
         "coverUrl": "https://covers.example.org/germinal.jpg"}
        """.formatted(isbn, totalCopies);
  }

  private Book createBook(String isbn, int copies) {
    Book book = new Book();
    book.setIsbn(isbn);
    book.setTitle("Livre de test");
    book.setAuthor("Auteur de test");
    book.setTotalCopies(copies);
    book.setAvailableCopies(copies);
    book.setCategory(categoryRepository.findById(1).orElseThrow());
    return bookRepository.saveAndFlush(book);
  }

  private void createLoan(Book book, LoanStatus status) {
    Loan loan = new Loan();
    loan.setUser(userRepository.findByEmailIgnoreCase(USER).orElseThrow());
    loan.setBook(book);
    loan.setStatus(status);
    if (status == LoanStatus.RETURNED) {
      loan.setReturnDate(LocalDate.now());
    }
    loanRepository.saveAndFlush(loan);
  }

  @Test
  void librarianCreatesBook() throws Exception {
    mockMvc.perform(post("/api/books")
            .header(HttpHeaders.AUTHORIZATION, bearer(LIBRARIAN))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bookJson("978-2-07-041080-4", 3)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.title").value("Germinal"))
        .andExpect(jsonPath("$.category.label").value("Roman"))
        .andExpect(jsonPath("$.totalCopies").value(3))
        .andExpect(jsonPath("$.availableCopies").value(3))
        .andExpect(jsonPath("$.coverUrl").value("https://covers.example.org/germinal.jpg"));

    assertThat(bookRepository.existsByIsbn("978-2-07-041080-4")).isTrue();
  }

  @Test
  void adminCanAlsoCreateBook() throws Exception {
    mockMvc.perform(post("/api/books")
            .header(HttpHeaders.AUTHORIZATION, bearer(ADMIN))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bookJson("978-2-07-041080-4", 1)))
        .andExpect(status().isCreated());
  }

  @Test
  void userGets403OnCreateAndUpdate() throws Exception {
    String token = bearer(USER);

    mockMvc.perform(post("/api/books")
            .header(HttpHeaders.AUTHORIZATION, token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(bookJson("978-2-07-041080-4", 1)))
        .andExpect(status().isForbidden())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

    mockMvc.perform(put("/api/books/1")
            .header(HttpHeaders.AUTHORIZATION, token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(bookJson("978-2-07-041080-4", 1)))
        .andExpect(status().isForbidden());
  }

  @Test
  void visitorGets401() throws Exception {
    mockMvc.perform(post("/api/books")
            .contentType(MediaType.APPLICATION_JSON)
            .content(bookJson("978-2-07-041080-4", 1)))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void isbnMustBeUniqueEvenWithoutDashes() throws Exception {
    mockMvc.perform(post("/api/books")
            .header(HttpHeaders.AUTHORIZATION, bearer(LIBRARIAN))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bookJson("9782070368228", 1)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("Cet ISBN est déjà utilisé par un autre livre du catalogue."));
  }

  @Test
  void invalidIsbnAndUnknownCategoryReturn400() throws Exception {
    String token = bearer(LIBRARIAN);

    mockMvc.perform(post("/api/books")
            .header(HttpHeaders.AUTHORIZATION, token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(bookJson("123-456", 1)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.isbn").exists());

    mockMvc.perform(post("/api/books")
            .header(HttpHeaders.AUTHORIZATION, token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(bookJson("978-2-07-041080-4", 1).replace("\"categoryId\": 1", "\"categoryId\": 999")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.categoryId").value("Cette catégorie n'existe pas."));
  }

  @Test
  void updateKeepsSameIsbnAndAdjustsAvailableCopies() throws Exception {
    Book book = createBook("978-0-00-000001-0", 2);
    createLoan(book, LoanStatus.ACTIVE);
    entityManager.clear();

    mockMvc.perform(put("/api/books/" + book.getId())
            .header(HttpHeaders.AUTHORIZATION, bearer(LIBRARIAN))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bookJson("978-0-00-000001-0", 5)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("Germinal"))
        .andExpect(jsonPath("$.totalCopies").value(5))
        .andExpect(jsonPath("$.availableCopies").value(4));
  }

  @Test
  void copiesCannotBeLowerThanOngoingLoans() throws Exception {
    Book book = createBook("978-0-00-000001-0", 2);
    createLoan(book, LoanStatus.ACTIVE);
    createLoan(book, LoanStatus.LATE);
    entityManager.clear();

    mockMvc.perform(put("/api/books/" + book.getId())
            .header(HttpHeaders.AUTHORIZATION, bearer(LIBRARIAN))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bookJson("978-0-00-000001-0", 1)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.totalCopies").value(
            "Il y a actuellement 2 exemplaire(s) emprunté(s) : le nombre d'exemplaires ne peut pas être inférieur."));
  }

  @Test
  void updateRefusesIsbnOfAnotherBook() throws Exception {
    Book book = createBook("978-0-00-000001-0", 1);

    mockMvc.perform(put("/api/books/" + book.getId())
            .header(HttpHeaders.AUTHORIZATION, bearer(LIBRARIAN))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bookJson("978-2-07-036822-8", 1)))
        .andExpect(status().isConflict());
  }

  @Test
  void updateUnknownBookReturns404() throws Exception {
    mockMvc.perform(put("/api/books/999999")
            .header(HttpHeaders.AUTHORIZATION, bearer(LIBRARIAN))
            .contentType(MediaType.APPLICATION_JSON)
            .content(bookJson("978-0-00-000001-0", 1)))
        .andExpect(status().isNotFound());
  }

  @Test
  void onlyAdminCanDelete() throws Exception {
    Book book = createBook("978-0-00-000001-0", 1);

    mockMvc.perform(delete("/api/books/" + book.getId()).header(HttpHeaders.AUTHORIZATION, bearer(LIBRARIAN)))
        .andExpect(status().isForbidden());
    mockMvc.perform(delete("/api/books/" + book.getId()).header(HttpHeaders.AUTHORIZATION, bearer(USER)))
        .andExpect(status().isForbidden());

    mockMvc.perform(delete("/api/books/" + book.getId()).header(HttpHeaders.AUTHORIZATION, bearer(ADMIN)))
        .andExpect(status().isNoContent());
    assertThat(bookRepository.findById(book.getId())).isEmpty();
  }

  @Test
  void deleteIsRefusedWhenLoanIsOngoing() throws Exception {
    Book book = createBook("978-0-00-000001-0", 1);
    createLoan(book, LoanStatus.ACTIVE);

    mockMvc.perform(delete("/api/books/" + book.getId()).header(HttpHeaders.AUTHORIZATION, bearer(ADMIN)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value(
            "Impossible de supprimer « Livre de test » : un emprunt est en cours sur ce livre."));
  }

  @Test
  void deleteIsRefusedWhenBookHasLoanHistory() throws Exception {
    Book book = createBook("978-0-00-000001-0", 1);
    createLoan(book, LoanStatus.RETURNED);

    mockMvc.perform(delete("/api/books/" + book.getId()).header(HttpHeaders.AUTHORIZATION, bearer(ADMIN)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value(
            "Impossible de supprimer « Livre de test » : ce livre a déjà été emprunté et son historique "
                + "est conservé pour les statistiques."));
  }

  @Test
  void librarianListShowsIsbnAndCopies() throws Exception {
    mockMvc.perform(get("/api/librarian/books").param("q", "petit prince")
            .header(HttpHeaders.AUTHORIZATION, bearer(LIBRARIAN)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].isbn").value("978-2-07-040850-4"))
        .andExpect(jsonPath("$.content[0].totalCopies").exists())
        .andExpect(jsonPath("$.content[0].availableCopies").exists());

    mockMvc.perform(get("/api/librarian/books").header(HttpHeaders.AUTHORIZATION, bearer(USER)))
        .andExpect(status().isForbidden());
  }

}
