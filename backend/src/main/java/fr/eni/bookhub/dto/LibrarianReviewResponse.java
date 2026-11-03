package fr.eni.bookhub.dto;

import fr.eni.bookhub.domain.Rating;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Avis affiché dans l'écran de modération bibliothécaire")
public record LibrarianReviewResponse(

    @Schema(description = "Identifiant de l'avis")
    Long id,

    @Schema(description = "Identifiant du livre")
    Long bookId,

    @Schema(description = "Titre du livre")
    String bookTitle,

    @Schema(description = "Nom affiché du lecteur")
    String authorLabel,

    @Schema(description = "Note de 1 à 5")
    short score,

    @Schema(description = "Commentaire, null si absent")
    String comment
) {

  public static LibrarianReviewResponse from(Rating rating) {
    return new LibrarianReviewResponse(
        rating.getId(),
        rating.getBook().getId(),
        rating.getBook().getTitle(),
        BookReviewResponse.from(rating).authorLabel(),
        rating.getScore(),
        rating.getComment());
  }

}
