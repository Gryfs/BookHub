package fr.eni.bookhub.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class BookSearchTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Test
  void searchIgnoresCase() throws Exception {
    mockMvc.perform(get("/api/books/search").param("q", "CAMUS"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(1)))
        .andExpect(jsonPath("$.content[0].title").value("L'Étranger"));
  }

  @Test
  void searchLooksInTitleAndAuthor() throws Exception {
    mockMvc.perform(get("/api/books/search").param("q", "harry"))
        .andExpect(jsonPath("$.content[*].title").value(containsInAnyOrder(
            "Harry Potter à l'école des sorciers", "La Vérité sur l'affaire Harry Quebert")));

    mockMvc.perform(get("/api/books/search").param("q", "asimov"))
        .andExpect(jsonPath("$.content[*].title").value(containsInAnyOrder("Fondation")));
  }

  @Test
  void searchByIsbnWithOrWithoutDashes() throws Exception {
    mockMvc.perform(get("/api/books/search").param("q", "978-2-07-036822"))
        .andExpect(jsonPath("$.content[*].title").value(containsInAnyOrder("L'Étranger")));

    mockMvc.perform(get("/api/books/search").param("q", "9782070368228"))
        .andExpect(jsonPath("$.content[*].title").value(containsInAnyOrder("L'Étranger")));
  }

  @Test
  void filterByCategory() throws Exception {
    mockMvc.perform(get("/api/books/search").param("categoryId", "2"))
        .andExpect(jsonPath("$.content[*].title").value(containsInAnyOrder("1984", "Dune", "Fondation")));
  }

  @Test
  void filterByAvailability() throws Exception {
    mockMvc.perform(get("/api/books/search").param("available", "false"))
        .andExpect(jsonPath("$.content[*].title").value(containsInAnyOrder("Dune", "Sapiens")));

    mockMvc.perform(get("/api/books/search").param("available", "true"))
        .andExpect(jsonPath("$.totalElements").value(10));
  }

  @Test
  void filtersAreCombined() throws Exception {
    mockMvc.perform(get("/api/books/search").param("q", "dune").param("available", "true"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content", hasSize(0)))
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  void sortByRatingPutsBestRatedFirst() throws Exception {
    mockMvc.perform(get("/api/books/search").param("sort", "RATING"))
        .andExpect(jsonPath("$.content[0].title").value("Fondation"))
        .andExpect(jsonPath("$.content[1].title").value("Le Petit Prince"))
        .andExpect(jsonPath("$.content[2].title").value("Le Crime de l'Orient-Express"))
        .andExpect(jsonPath("$.content[3].title").value("1984"))
        .andExpect(jsonPath("$.totalElements").value(12));
  }

  @Test
  void sortByRecentPutsLastAddedFirst() throws Exception {
    mockMvc.perform(get("/api/books/search").param("sort", "RECENT"))
        .andExpect(jsonPath("$.content[0].title").value("La Vérité sur l'affaire Harry Quebert"));
  }

  @Test
  void sortAlphabeticalByDefault() throws Exception {
    mockMvc.perform(get("/api/books/search"))
        .andExpect(jsonPath("$.content[0].title").value("1984"))
        .andExpect(jsonPath("$.content[1].title").value("Astérix le Gaulois"));
  }

  @Test
  void specialCharactersAreSearchedLiterally() throws Exception {
    mockMvc.perform(get("/api/books/search").param("q", "%"))
        .andExpect(jsonPath("$.totalElements").value(0));
  }

  @Test
  void invalidParametersReturn400() throws Exception {
    mockMvc.perform(get("/api/books/search").param("sort", "PRIX")).andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/books/search").param("size", "500")).andExpect(status().isBadRequest());
    mockMvc.perform(get("/api/books/search").param("page", "-1")).andExpect(status().isBadRequest());
  }

  @Test
  void categoriesArePublicAndSorted() throws Exception {
    mockMvc.perform(get("/api/categories"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(6)))
        .andExpect(jsonPath("$[0].label").value("Bande dessinée"));
  }

  @Test
  @Transactional
  void searchUsesTrigramIndexes() {
    jdbcTemplate.execute("SET LOCAL enable_seqscan = off");
    List<String> plan = jdbcTemplate.queryForList("""
        EXPLAIN SELECT id_livre FROM livre
         WHERE titre ILIKE '%camus%' OR auteur ILIKE '%camus%' OR isbn ILIKE '%camus%'
        """, String.class);

    String fullPlan = String.join("\n", plan);
    assertThat(fullPlan)
        .contains("ix_livre_titre_trgm")
        .contains("ix_livre_auteur_trgm")
        .contains("ix_livre_isbn_trgm")
        .doesNotContain("Seq Scan");
  }

  @Test
  void searchRespondsInLessThanOneSecond() throws Exception {
    mockMvc.perform(get("/api/books/search").param("q", "camus"));

    long start = System.currentTimeMillis();
    mockMvc.perform(get("/api/books/search").param("q", "harry").param("categoryId", "4")
            .param("available", "true").param("sort", "RATING"))
        .andExpect(status().isOk());
    long duration = System.currentTimeMillis() - start;

    assertThat(duration).isLessThan(1000);
  }

}
