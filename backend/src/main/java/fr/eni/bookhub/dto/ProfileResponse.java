package fr.eni.bookhub.dto;

import fr.eni.bookhub.domain.Role;
import fr.eni.bookhub.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informations personnelles affichées sur la page Mon profil")
public record ProfileResponse(

    @Schema(description = "Identifiant de l'utilisateur", example = "3")
    Long id,

    @Schema(description = "Adresse email (non modifiable)", example = "marie.lectrice@example.fr")
    String email,

    @Schema(description = "Prénom", example = "Marie")
    String firstName,

    @Schema(description = "Nom", example = "Dupont")
    String lastName,

    @Schema(description = "Téléphone, null s'il n'est pas renseigné", example = "06 11 12 13 14")
    String phone,

    @Schema(description = "Nom affiché sur les avis, null pour afficher le nom réel", example = "MarieLit")
    String pseudonym,

    @Schema(description = "Rôle : USER (lecteur), LIBRARIAN (bibliothécaire) ou ADMIN", example = "USER")
    Role role
) {

  public static ProfileResponse from(User user) {
    return new ProfileResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
        user.getPhone(), user.getPseudonym(), user.getRole());
  }

}
