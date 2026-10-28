package fr.eni.bookhub.dto;

import fr.eni.bookhub.domain.Role;
import fr.eni.bookhub.domain.User;

public record UserResponse(Long id, String email, String firstName, String lastName, String pseudonym,
    Role role) {

  public static UserResponse from(User user) {
    return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(),
        user.getPseudonym(), user.getRole());
  }

}
