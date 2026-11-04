package fr.eni.bookhub.dto;

import fr.eni.bookhub.domain.Role;
import fr.eni.bookhub.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Utilisateur pour l'administration des rôles")
public record UserAdminResponse(
    Long id,
    String email,
    String firstName,
    String lastName,
    Role role
) {

  public static UserAdminResponse from(User user) {
    return new UserAdminResponse(
        user.getId(),
        user.getEmail(),
        user.getFirstName(),
        user.getLastName(),
        user.getRole());
  }

}
