package fr.eni.bookhub.web;

import fr.eni.bookhub.config.OpenApiConfig;
import fr.eni.bookhub.dto.BookReviewResponse;
import fr.eni.bookhub.dto.ErrorResponse;
import fr.eni.bookhub.dto.ReaderRatingContext;
import fr.eni.bookhub.dto.UpsertRatingRequest;
import fr.eni.bookhub.service.RatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
@Tag(name = "Avis", description = "Notation et commentaires des lecteurs (US-RATE-01/02).")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class RatingController {

  private final RatingService ratingService;

  @GetMapping("/books/{bookId}/context")
  @Operation(summary = "Contexte de notation sur un livre",
      description = "Indique si le lecteur peut publier un avis et renvoie l'avis existant le cas échéant.")
  public ReaderRatingContext context(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
      @PathVariable Long bookId) {
    return ratingService.contextFor(principal.getUsername(), bookId);
  }

  @PutMapping
  @Operation(summary = "Publier ou mettre à jour un avis (US-RATE-01/02)",
      description = """
          Crée ou remplace l'avis du lecteur sur un livre (un seul avis par ouvrage).
          Nécessite un emprunt déjà rendu sur ce titre.
          """)
  @ApiResponse(responseCode = "200", description = "Avis enregistré")
  @ApiResponse(responseCode = "409", description = "Règle métier non respectée",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public BookReviewResponse upsert(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
      @Valid @RequestBody UpsertRatingRequest request) {
    return ratingService.upsert(principal.getUsername(), request);
  }

}
