package fr.eni.bookhub.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class SecurityRbacTest {

  private static final String PASSWORD = "BookHub!2026a";

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
  void userCannotAccessLibrarianRoute() throws Exception {
    mockMvc.perform(get("/api/librarian/access-check")
            .header("Authorization", bearer("marie.lectrice@example.fr")))
        .andExpect(status().isForbidden());
  }

  @Test
  void librarianCanAccessLibrarianRoute() throws Exception {
    mockMvc.perform(get("/api/librarian/access-check")
            .header("Authorization", bearer("ahmed.biblio@quartier-solidaire.fr")))
        .andExpect(status().isOk());
  }

}
