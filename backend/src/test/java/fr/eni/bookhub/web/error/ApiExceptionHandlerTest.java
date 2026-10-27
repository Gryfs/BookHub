package fr.eni.bookhub.web.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import fr.eni.bookhub.service.exception.BusinessRuleException;
import fr.eni.bookhub.service.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(controllers = ApiExceptionHandlerTest.TestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({ApiExceptionHandler.class, ApiExceptionHandlerTest.TestController.class})
class ApiExceptionHandlerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void ressourceIntrouvable_renvoie404() throws Exception {
    mockMvc.perform(get("/test/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("Livre introuvable (id 42)"));
  }

  @Test
  void regleDeGestion_renvoie409() throws Exception {
    mockMvc.perform(get("/test/business"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.detail").value("Vous avez déjà 3 emprunts en cours."));
  }

  @Test
  void corpsInvalide_renvoie400AvecErreursParChamp() throws Exception {
    mockMvc.perform(post("/test/valid").contentType(MediaType.APPLICATION_JSON).content("{\"titre\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.title").value("Données invalides"))
        .andExpect(jsonPath("$.errors.titre").value("Le titre est obligatoire"));
  }

  @Test
  void jsonIllisible_renvoie400() throws Exception {
    mockMvc.perform(post("/test/valid").contentType(MediaType.APPLICATION_JSON).content("{"))
        .andExpect(status().isBadRequest())
        .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
  }

  @Test
  void accesRefuse_renvoie403() throws Exception {
    mockMvc.perform(get("/test/forbidden"))
        .andExpect(status().isForbidden());
  }

  @Test
  void erreurInattendue_renvoie500SansDetailTechnique() throws Exception {
    mockMvc.perform(get("/test/boom"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.detail").value("Une erreur inattendue est survenue."));
  }

  record LivreRequest(@NotBlank(message = "Le titre est obligatoire") String titre) {
  }

  @RestController
  static class TestController {

    @GetMapping("/test/not-found")
    void notFound() {
      throw new ResourceNotFoundException("Livre", 42);
    }

    @GetMapping("/test/business")
    void business() {
      throw new BusinessRuleException("Vous avez déjà 3 emprunts en cours.");
    }

    @PostMapping("/test/valid")
    void valid(@Valid @RequestBody LivreRequest request) {
    }

    @GetMapping("/test/forbidden")
    void forbidden() {
      throw new AccessDeniedException("refusé");
    }

    @GetMapping("/test/boom")
    void boom() {
      throw new IllegalStateException("détail interne");
    }

  }

}
