package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Droit de notation et avis déjà publié par le lecteur connecté")
public record ReaderRatingContext(

    @Schema(description = "true si le lecteur a déjà rendu ce livre (ou possède déjà un avis)")
    boolean canRate,

    @Schema(description = "Motif si canRate est false")
    String reason,

    @Schema(description = "Avis existant, null si aucun")
    BookReviewResponse myReview
) {

  public static ReaderRatingContext blocked(String reason) {
    return new ReaderRatingContext(false, reason, null);
  }

  public static ReaderRatingContext allowed(BookReviewResponse myReview) {
    return new ReaderRatingContext(true, null, myReview);
  }

}
