package fr.eni.bookhub.dto;

import fr.eni.bookhub.domain.Rating;
import fr.eni.bookhub.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Avis d'un lecteur sur un livre (note et commentaire optionnel)")
public record BookReviewResponse(

    @Schema(description = "Identifiant de l'avis", example = "12")
    Long id,

    @Schema(description = "Nom affiché (pseudonyme ou prénom + initiale)", example = "MarieLit")
    String authorLabel,

    @Schema(description = "Note de 1 à 5", example = "4")
    short score,

    @Schema(description = "Commentaire texte, null s'il n'y en a pas")
    String comment
) {

  public static BookReviewResponse from(Rating rating) {
    return new BookReviewResponse(
        rating.getId(),
        reviewerLabel(rating.getUser()),
        rating.getScore(),
        rating.getComment());
  }

  private static String reviewerLabel(User user) {
    if (user.getPseudonym() != null && !user.getPseudonym().isBlank()) {
      return user.getPseudonym();
    }
    String lastName = user.getLastName();
    if (lastName != null && !lastName.isBlank()) {
      return user.getFirstName() + " " + lastName.charAt(0) + ".";
    }
    return user.getFirstName();
  }

}
