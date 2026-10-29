package fr.eni.bookhub.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void catalogueIsPublicAndSortedByTitle() throws Exception {
    mockMvc.perform(get("/api/books"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(12)))
        .andExpect(jsonPath("$.content[0].title").value("1984"))
        .andExpect(jsonPath("$.content[1].title").value("Astérix le Gaulois"))
        .andExpect(jsonPath("$.page").value(0))
        .andExpect(jsonPath("$.size").value(20))
        .andExpect(jsonPath("$.totalElements").value(12))
        .andExpect(jsonPath("$.totalPages").value(1));
  }

  @Test
  void catalogueReturnsLightBookWithAvailability() throws Exception {
    mockMvc.perform(get("/api/books").param("sort", "title"))
        .andExpect(jsonPath("$.content[?(@.title == 'Dune')].available").value(false))
        .andExpect(jsonPath("$.content[?(@.title == '1984')].available").value(true))
        .andExpect(jsonPath("$.content[0].author").value("George Orwell"))
        .andExpect(jsonPath("$.content[0].description").doesNotExist())
        .andExpect(jsonPath("$.content[0].isbn").doesNotExist())
        .andExpect(jsonPath("$.content[0].category").doesNotExist());
  }

  @Test
  void cataloguePagination() throws Exception {
    mockMvc.perform(get("/api/books").param("page", "1").param("size", "5"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(5)))
        .andExpect(jsonPath("$.page").value(1))
        .andExpect(jsonPath("$.totalPages").value(3));
  }

  @Test
  void pageSizeIsCappedAt100() throws Exception {
    mockMvc.perform(get("/api/books").param("size", "5000"))
        .andExpect(jsonPath("$.size").value(100));
  }

  @Test
  void unknownSortPropertyReturns400() throws Exception {
    mockMvc.perform(get("/api/books").param("sort", "prix"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.detail").value("Tri impossible sur le champ « prix »."));
  }

  @Test
  void catalogueStillWorksWithAnExpiredOrInvalidToken() throws Exception {
    mockMvc.perform(get("/api/books").header("Authorization", "Bearer jeton.invalide"))
        .andExpect(status().isOk());
  }

}
