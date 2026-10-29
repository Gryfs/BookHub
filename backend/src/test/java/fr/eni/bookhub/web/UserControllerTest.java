package fr.eni.bookhub.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import fr.eni.bookhub.domain.User;
import fr.eni.bookhub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

  private static final String PASSWORD = "BookHub!2026a";
  private static final String MARIE = "marie.lectrice@example.fr";
  private static final String AHMED = "ahmed.biblio@quartier-solidaire.fr";

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;

  private ResultActions login(String email, String password) throws Exception {
    return mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {"email": "%s", "password": "%s"}
            """.formatted(email, password)));
  }

  private String bearer(String email) throws Exception {
    String response = login(email, PASSWORD).andReturn().getResponse().getContentAsString();
    return "Bearer " + JsonPath.read(response, "$.token");
  }

  private String profileJson(String firstName, String lastName, String phone, String pseudonym) {
    return """
        {"firstName": "%s", "lastName": "%s", "phone": "%s", "pseudonym": "%s"}
        """.formatted(firstName, lastName, phone, pseudonym);
  }

  private String passwordJson(String currentPassword, String newPassword) {
    return """
        {"currentPassword": "%s", "newPassword": "%s"}
        """.formatted(currentPassword, newPassword);
  }

  @Test
  void getProfileReturnsPersonalInformation() throws Exception {
    mockMvc.perform(get("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer(MARIE)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Marie"))
        .andExpect(jsonPath("$.phone").value("0611121314"))
        .andExpect(jsonPath("$.pseudonym").value("MarieLit"))
        .andExpect(jsonPath("$.passwordHash").doesNotExist());
  }

  @Test
  void loginResponseDoesNotContainPhoneOrPseudonym() throws Exception {
    login(MARIE, PASSWORD)
        .andExpect(jsonPath("$.user.phone").doesNotExist())
        .andExpect(jsonPath("$.user.pseudonym").doesNotExist());
  }

  @Test
  void updateProfileChangesNamePhoneAndPseudonym() throws Exception {
    mockMvc.perform(put("/api/users/me")
            .header(HttpHeaders.AUTHORIZATION, bearer(MARIE))
            .contentType(MediaType.APPLICATION_JSON)
            .content(profileJson(" Marion ", "Durand", "06 99 88 77 66", "MarionLit")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Marion"))
        .andExpect(jsonPath("$.phone").value("06 99 88 77 66"))
        .andExpect(jsonPath("$.pseudonym").value("MarionLit"));

    User marie = userRepository.findByEmailIgnoreCase(MARIE).orElseThrow();
    assertThat(marie.getLastName()).isEqualTo("Durand");
  }

  @Test
  void emptyPhoneAndPseudonymAreRemoved() throws Exception {
    mockMvc.perform(put("/api/users/me")
            .header(HttpHeaders.AUTHORIZATION, bearer(MARIE))
            .contentType(MediaType.APPLICATION_JSON)
            .content(profileJson("Marie", "Dupont", "", "")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.phone").isEmpty())
        .andExpect(jsonPath("$.pseudonym").isEmpty());
  }

  @Test
  void updateProfileWithInvalidDataReturns400() throws Exception {
    mockMvc.perform(put("/api/users/me")
            .header(HttpHeaders.AUTHORIZATION, bearer(MARIE))
            .contentType(MediaType.APPLICATION_JSON)
            .content(profileJson("", "Dupont", "12", "MarieLit")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.firstName").exists())
        .andExpect(jsonPath("$.errors.phone").exists());
  }

  @Test
  void changePasswordWithWrongCurrentPasswordReturns400() throws Exception {
    mockMvc.perform(put("/api/users/me/password")
            .header(HttpHeaders.AUTHORIZATION, bearer(MARIE))
            .contentType(MediaType.APPLICATION_JSON)
            .content(passwordJson("MauvaisMotDePasse!1", "NouveauMotDePasse!1")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.currentPassword").value("Le mot de passe actuel est incorrect."));
  }

  @Test
  void changePasswordWithWeakPasswordReturns400() throws Exception {
    mockMvc.perform(put("/api/users/me/password")
            .header(HttpHeaders.AUTHORIZATION, bearer(MARIE))
            .contentType(MediaType.APPLICATION_JSON)
            .content(passwordJson(PASSWORD, "faible")))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.newPassword").exists());
  }

  @Test
  void changePasswordReplacesOldPassword() throws Exception {
    mockMvc.perform(put("/api/users/me/password")
            .header(HttpHeaders.AUTHORIZATION, bearer(MARIE))
            .contentType(MediaType.APPLICATION_JSON)
            .content(passwordJson(PASSWORD, "NouveauMotDePasse!1")))
        .andExpect(status().isNoContent());

    login(MARIE, PASSWORD).andExpect(status().isUnauthorized());
    login(MARIE, "NouveauMotDePasse!1").andExpect(status().isOk());
  }

  @Test
  void deleteAccountIsRefusedWhenLoanIsActive() throws Exception {
    mockMvc.perform(delete("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer(MARIE)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail")
            .value("Vous ne pouvez pas supprimer votre compte tant qu'un emprunt est en cours."));
  }

  @Test
  void deleteAccountIsRefusedWhenLoanIsLate() throws Exception {
    mockMvc.perform(delete("/api/users/me")
            .header(HttpHeaders.AUTHORIZATION, bearer("lucas.lecteur@example.fr")))
        .andExpect(status().isConflict());
  }

  @Test
  void deleteAccountAnonymizesUser() throws Exception {
    Long ahmedId = userRepository.findByEmailIgnoreCase(AHMED).orElseThrow().getId();

    mockMvc.perform(delete("/api/users/me").header(HttpHeaders.AUTHORIZATION, bearer(AHMED)))
        .andExpect(status().isNoContent());

    login(AHMED, PASSWORD).andExpect(status().isUnauthorized());
    assertThat(userRepository.findByEmailIgnoreCase("deleted-" + ahmedId + "@anonymized.invalid"))
        .isPresent();
  }

  @Test
  void profileRequiresAuthentication() throws Exception {
    mockMvc.perform(get("/api/users/me")).andExpect(status().isUnauthorized());
  }

}
