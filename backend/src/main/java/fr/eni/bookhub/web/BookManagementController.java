package fr.eni.bookhub.web;

import fr.eni.bookhub.config.OpenApiConfig;
import fr.eni.bookhub.dto.BookAdminResponse;
import fr.eni.bookhub.dto.BookDetailResponse;
import fr.eni.bookhub.dto.BookRequest;
import fr.eni.bookhub.dto.ErrorResponse;
import fr.eni.bookhub.dto.PageResponse;
import fr.eni.bookhub.service.BookManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Gestion du catalogue",
    description = "Ajout, modification et suppression des livres. Ajout et modification : bibliothécaire "
        + "(LIBRARIAN) ou administrateur. Suppression : administrateur (ADMIN) uniquement.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@ApiResponse(responseCode = "401", description = "Jeton absent, invalide ou expiré",
    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(value = """
            {"type": "about:blank", "title": "Non authentifié", "status": 401,
             "detail": "Authentification requise.", "instance": "/api/books"}
            """)))
@ApiResponse(responseCode = "403", description = "Rôle insuffisant (un lecteur USER ne peut pas gérer le catalogue)",
    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(value = """
            {"type": "about:blank", "title": "Accès refusé", "status": 403,
             "detail": "Vous n'avez pas les droits nécessaires pour cette action.", "instance": "/api/books"}
            """)))
public class BookManagementController {

  private final BookManagementService bookManagementService;

  @GetMapping("/librarian/books")
  @PreAuthorize("hasRole('LIBRARIAN')")
  @Operation(summary = "Lister les livres pour la gestion du catalogue",
      description = """
          Sert au tableau de la page **Gestion du catalogue** (bibliothécaire).

          - Accès : LIBRARIAN ou ADMIN.
          - Contrairement au catalogue public, chaque ligne contient l'ISBN et le nombre d'exemplaires \
          (total et disponibles).
          - Recherche facultative sur le titre, l'auteur et l'ISBN, tri par titre, 20 livres par page.
          """)
  @ApiResponse(responseCode = "200", description = "Page de livres")
  public PageResponse<BookAdminResponse> getBooks(
      @Parameter(description = "Texte cherché dans le titre, l'auteur et l'ISBN", example = "prince")
      @RequestParam(required = false) @Size(max = 100) String q,
      @Parameter(description = "Numéro de page, en commençant à 0")
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @Parameter(description = "Nombre de livres par page (100 maximum)")
      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
    return bookManagementService.getBooks(q, page, size);
  }

  @PostMapping("/books")
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasRole('LIBRARIAN')")
  @Operation(summary = "Ajouter un livre au catalogue",
      description = """
          Sert au bouton **Enregistrer** du formulaire d'ajout d'un ouvrage.

          - Accès : LIBRARIAN ou ADMIN. Un lecteur (USER) reçoit 403.
          - L'ISBN doit être unique : la comparaison ignore les tirets.
          - Tous les exemplaires sont disponibles à la création.
          - La couverture est une adresse d'image (http ou https), facultative.
          """)
  @ApiResponse(responseCode = "201", description = "Livre créé, renvoie sa fiche complète")
  @ApiResponse(responseCode = "400", description = "Champs invalides (ISBN mal formé, catégorie inconnue...)",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
          examples = @ExampleObject(value = """
              {"type": "about:blank", "title": "Données invalides", "status": 400,
               "detail": "Un ou plusieurs champs sont invalides.", "instance": "/api/books",
               "errors": {"isbn": "L'ISBN doit contenir 13 chiffres, éventuellement séparés par des tirets."}}
              """)))
  @ApiResponse(responseCode = "409", description = "ISBN déjà utilisé par un autre livre",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
          examples = @ExampleObject(value = """
              {"type": "about:blank", "title": "Règle de gestion non respectée", "status": 409,
               "detail": "Cet ISBN est déjà utilisé par un autre livre du catalogue.", "instance": "/api/books"}
              """)))
  public BookDetailResponse createBook(@Valid @RequestBody BookRequest request) {
    return bookManagementService.createBook(request);
  }

  @PutMapping("/books/{id}")
  @PreAuthorize("hasRole('LIBRARIAN')")
  @Operation(summary = "Modifier un livre",
      description = """
          Sert au bouton **Enregistrer** du formulaire de modification d'un ouvrage.

          - Accès : LIBRARIAN ou ADMIN. Un lecteur (USER) reçoit 403.
          - L'ISBN doit rester unique (le livre lui-même est exclu de la vérification).
          - Le nombre d'exemplaires ne peut pas descendre sous le nombre d'exemplaires actuellement empruntés. \
          L'écart est reporté automatiquement sur les exemplaires disponibles.
          """)
  @ApiResponse(responseCode = "200", description = "Livre modifié, renvoie sa fiche à jour")
  @ApiResponse(responseCode = "400", description = "Champs invalides, ou exemplaires inférieurs aux emprunts en cours",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
          examples = @ExampleObject(value = """
              {"type": "about:blank", "title": "Données invalides", "status": 400,
               "detail": "Un ou plusieurs champs sont invalides.", "instance": "/api/books/9",
               "errors": {"totalCopies": "Il y a actuellement 2 exemplaire(s) emprunté(s) : le nombre d'exemplaires ne peut pas être inférieur."}}
              """)))
  @ApiResponse(responseCode = "404", description = "Livre inconnu",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "409", description = "ISBN déjà utilisé par un autre livre",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
  public BookDetailResponse updateBook(
      @Parameter(description = "Identifiant du livre", example = "9") @PathVariable long id,
      @Valid @RequestBody BookRequest request) {
    return bookManagementService.updateBook(id, request);
  }

  @DeleteMapping("/books/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasRole('ADMIN')")
  @Operation(summary = "Supprimer un livre du catalogue",
      description = """
          Sert au bouton **Confirmer** de la page de suppression d'un ouvrage.

          - Accès : **ADMIN uniquement**. Un bibliothécaire ou un lecteur reçoit 403.
          - Refusé si un emprunt est en cours sur ce livre.
          - Refusé aussi si le livre a déjà été emprunté : son historique est conservé pour les statistiques.
          - Les réservations et avis éventuels du livre sont supprimés avec lui.
          """)
  @ApiResponse(responseCode = "204", description = "Livre supprimé")
  @ApiResponse(responseCode = "404", description = "Livre inconnu",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
  @ApiResponse(responseCode = "409", description = "Suppression refusée : emprunt en cours ou historique d'emprunts",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
          examples = @ExampleObject(value = """
              {"type": "about:blank", "title": "Règle de gestion non respectée", "status": 409,
               "detail": "Impossible de supprimer « 1984 » : un emprunt est en cours sur ce livre.",
               "instance": "/api/books/9"}
              """)))
  public void deleteBook(@Parameter(description = "Identifiant du livre", example = "9") @PathVariable long id) {
    bookManagementService.deleteBook(id);
  }

}
