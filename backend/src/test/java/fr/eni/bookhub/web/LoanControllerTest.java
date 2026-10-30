package fr.eni.bookhub.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
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
class LoanControllerTest {

  private static final String PASSWORD = "BookHub!2026a";
  private static final String MARIE = "marie.lectrice@example.fr";
  private static final String INES = "ines.lectrice@example.fr";

  @Autowired
  private MockMvc mockMvc;

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
    mockMvc.perform(post("/api/loans")
            .header("Authorization", bearer(INES))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"bookId\": 7}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.bookTitle").value("Les Misérables"))
        .andExpect(jsonPath("$.dueDate").exists())
        .andExpect(jsonPath("$.status").value("ACTIVE"));
  }

  @Test
  void borrowUnavailableBookReturns409() throws Exception {
    mockMvc.perform(post("/api/loans")
            .header("Authorization", bearer(INES))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"bookId\": 8}"))
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
