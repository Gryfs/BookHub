package fr.eni.bookhub.dto;

import fr.eni.bookhub.domain.Role;
import fr.eni.bookhub.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informations minimales sur un utilisateur (sans téléphone ni mot de passe)")
public record UserResponse(

    @Schema(description = "Identifiant de l'utilisateur", example = "3")
    Long id,

    @Schema(description = "Adresse email", example = "marie.lectrice@example.fr")
    String email,

    @Schema(description = "Prénom", example = "Marie")
    String firstName,

    @Schema(description = "Nom", example = "Dupont")
    String lastName,

    @Schema(description = "Rôle : USER (lecteur), LIBRARIAN (bibliothécaire) ou ADMIN", example = "USER")
    Role role
) {

  public static UserResponse from(User user) {
    return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
        user.getRole());
  }

}
