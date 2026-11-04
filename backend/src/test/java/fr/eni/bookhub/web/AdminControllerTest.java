package fr.eni.bookhub.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import fr.eni.bookhub.domain.Role;
import fr.eni.bookhub.domain.User;
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
class AdminControllerTest {

  private static final String PASSWORD = "BookHub!2026a";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

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

  @Test
  void adminCanLoadDashboardAndUsers() throws Exception {
    String token = bearer("sophie.admin@quartier-solidaire.fr");

    mockMvc.perform(get("/api/admin/dashboard").header("Authorization", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.stats.bookCount").isNumber())
        .andExpect(jsonPath("$.userCount").isNumber());

    mockMvc.perform(get("/api/admin/users").header("Authorization", token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].email").exists());
  }

  @Test
  void userCannotAccessAdminApi() throws Exception {
    mockMvc.perform(get("/api/admin/dashboard")
            .header("Authorization", bearer("marie.lectrice@example.fr")))
        .andExpect(status().isForbidden());
  }

  @Test
  void cannotDemoteLastAdmin() throws Exception {
    User backupAdmin = new User();
    backupAdmin.setEmail("backup.admin@example.fr");
    backupAdmin.setPasswordHash(passwordEncoder.encode(PASSWORD));
    backupAdmin.setFirstName("Backup");
    backupAdmin.setLastName("Admin");
    backupAdmin.setRole(Role.ADMIN);
    backupAdmin = userRepository.save(backupAdmin);

    User sophie = userRepository.findByEmailIgnoreCase("sophie.admin@quartier-solidaire.fr").orElseThrow();

    String backupToken = bearer(backupAdmin.getEmail());

    mockMvc.perform(put("/api/admin/users/{id}/role", sophie.getId())
            .header("Authorization", backupToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"role\": \"LIBRARIAN\"}"))
        .andExpect(status().isOk());

    mockMvc.perform(put("/api/admin/users/{id}/role", backupAdmin.getId())
            .header("Authorization", backupToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"role\": \"USER\"}"))
        .andExpect(status().isConflict());
  }

}
