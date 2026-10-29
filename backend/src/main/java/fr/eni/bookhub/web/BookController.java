package fr.eni.bookhub.web;

import fr.eni.bookhub.dto.BookSummaryResponse;
import fr.eni.bookhub.dto.ErrorResponse;
import fr.eni.bookhub.dto.PageResponse;
import fr.eni.bookhub.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
