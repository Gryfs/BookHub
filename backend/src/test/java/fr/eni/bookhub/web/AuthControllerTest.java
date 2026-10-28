package fr.eni.bookhub.web;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
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
class AuthControllerTest {

  private static final String VALID_PASSWORD = "BookHub!2026a";

  @Autowired
  private MockMvc mockMvc;

  private String registerJson(String email, String password, boolean consent) {
    return """
        {"firstName": "Jean", "lastName": "Test", "email": "%s", "password": "%s", "consent": %s}
        """.formatted(email, password, consent);
  }

  private String loginJson(String email, String password) {
    return """
        {"email": "%s", "password": "%s"}
        """.formatted(email, password);
  }

  @Test
  void registerCreatesUserWithRoleUser() throws Exception {
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerJson("Jean.Test@Example.fr", VALID_PASSWORD, true)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.email").value("jean.test@example.fr"))
        .andExpect(jsonPath("$.role").value("USER"))
        .andExpect(jsonPath("$.passwordHash").doesNotExist());
  }

  @Test
  void registerWithExistingEmailReturns409() throws Exception {
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerJson("MARIE.LECTRICE@example.fr", VALID_PASSWORD, true)))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("Cette adresse email est déjà utilisée."));
  }

  @Test
  void registerWithWeakPasswordReturns400() throws Exception {
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerJson("jean.test@example.fr", "motdepasse", true)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.password").exists());
  }

  @Test
  void registerWithoutConsentReturns400() throws Exception {
    mockMvc.perform(post("/api/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(registerJson("jean.test@example.fr", VALID_PASSWORD, false)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.errors.consent").exists());
  }

  @Test
  void loginReturnsTokenThatGivesAccessToMe() throws Exception {
    String response = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginJson("marie.lectrice@example.fr", VALID_PASSWORD)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value(not(emptyString())))
        .andExpect(jsonPath("$.user.firstName").value("Marie"))
        .andReturn().getResponse().getContentAsString();
    String token = JsonPath.read(response, "$.token");

    mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("marie.lectrice@example.fr"))
        .andExpect(jsonPath("$.role").value("USER"));
  }

  @Test
  void loginWithWrongPasswordReturns401WithGenericMessage() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginJson("marie.lectrice@example.fr", "MauvaisMotDePasse!1")))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Email ou mot de passe incorrect."));
  }

  @Test
  void loginWithUnknownEmailReturnsSameMessage() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginJson("inconnu@example.fr", VALID_PASSWORD)))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.detail").value("Email ou mot de passe incorrect."));
  }

  @Test
  void meWithoutTokenReturns401ProblemDetail() throws Exception {
    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isUnauthorized())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(401));
  }

  @Test
  void meWithInvalidTokenReturns401() throws Exception {
    mockMvc.perform(get("/api/auth/me").header(HttpHeaders.AUTHORIZATION, "Bearer faux.jeton.jwt"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void corsAllowsAngularFrontend() throws Exception {
    mockMvc.perform(options("/api/auth/login")
            .header(HttpHeaders.ORIGIN, "http://localhost:4200")
            .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:4200"));
  }

}
