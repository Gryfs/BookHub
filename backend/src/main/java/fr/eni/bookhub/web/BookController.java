package fr.eni.bookhub.web;

import fr.eni.bookhub.dto.BookSort;
import fr.eni.bookhub.dto.BookSummaryResponse;
import fr.eni.bookhub.dto.ErrorResponse;
import fr.eni.bookhub.dto.PageResponse;
import fr.eni.bookhub.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Catalogue", description = "Consultation des livres de la bibliothèque. La lecture est publique, "
    + "sans compte.")
public class BookController {

  private final BookService bookService;

  @GetMapping
  @Operation(summary = "Lister les livres du catalogue (paginé)",
      description = """
          Sert à la page **Catalogue** : renvoie une page de livres pour la grille.

          - Accès : public, aucun jeton nécessaire (un visiteur peut parcourir le catalogue).
          - **20 livres par page** par défaut, triés par titre.
          - Paramètres : `page` (commence à 0), `size` (100 maximum) et `sort` (ex. `title,asc` ou `author,desc`).
          - Chaque livre est renvoyé en version allégée : titre, auteur, couverture et disponibilité. \
          Le détail complet sera fourni par `GET /api/books/{id}`.
          """)
  @ApiResponse(responseCode = "200", description = "Page de livres")
  @ApiResponse(responseCode = "400", description = "Paramètre de tri ou de pagination invalide",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
          examples = @ExampleObject(value = """
              {"type": "about:blank", "title": "Paramètre invalide", "status": 400,
               "detail": "Tri impossible sur le champ « prix ».", "instance": "/api/books"}
              """)))
  public PageResponse<BookSummaryResponse> getCatalogue(
      @ParameterObject @PageableDefault(size = 20, sort = "title", direction = Sort.Direction.ASC) Pageable pageable) {
    return bookService.getCatalogue(pageable);
  }

  @GetMapping("/search")
  @Operation(summary = "Rechercher et filtrer les livres",
      description = """
          Sert à la page **Recherche** : trouve les livres d'après un texte, avec des filtres et un tri.

          - Accès : public, aucun jeton nécessaire.
          - Le texte est cherché dans le **titre**, l'**auteur** et l'**ISBN**, sans tenir compte de la casse \
          (« CAMUS » trouve « Albert Camus »). Un ISBN peut être saisi avec ou sans tirets.
          - Tous les paramètres sont facultatifs : sans texte ni filtre, tous les livres sont renvoyés.
          - Les filtres se cumulent : texte ET catégorie ET disponibilité.
          - Recherche indexée (index trigrammes PostgreSQL sur le titre, l'auteur et l'ISBN), paginée par 20.
          - Aucun résultat : la réponse est une page vide (`content` vide, `totalElements` à 0), pas une erreur.
          """)
  @ApiResponse(responseCode = "200", description = "Page de résultats, éventuellement vide")
  @ApiResponse(responseCode = "400", description = "Paramètre invalide (tri inconnu, page négative, taille hors limites...)",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
          examples = @ExampleObject(value = """
              {"type": "about:blank", "title": "Bad Request", "status": 400,
               "detail": "Failed to convert 'sort' with value: 'PRIX'", "instance": "/api/books/search"}
              """)))
  public PageResponse<BookSummaryResponse> search(
      @Parameter(description = "Texte cherché dans le titre, l'auteur et l'ISBN", example = "camus")
      @RequestParam(required = false) @Size(max = 100) String q,
      @Parameter(description = "Identifiant de catégorie (voir GET /api/categories)", example = "1")
      @RequestParam(required = false) Integer categoryId,
      @Parameter(description = "true : seulement les livres empruntables, false : seulement les indisponibles. "
          + "Absent : tous")
      @RequestParam(required = false) Boolean available,
      @Parameter(description = "Tri : TITLE (alphabétique), RATING (mieux notés), RECENT (derniers ajoutés)")
      @RequestParam(defaultValue = "TITLE") BookSort sort,
      @Parameter(description = "Numéro de page, en commençant à 0")
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @Parameter(description = "Nombre de livres par page (100 maximum)")
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return bookService.search(q, categoryId, available, sort, page, size);
  }

}
