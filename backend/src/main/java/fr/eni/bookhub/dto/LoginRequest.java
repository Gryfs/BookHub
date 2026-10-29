package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Identifiants saisis dans le formulaire de connexion")
public record LoginRequest(

    @Schema(description = "Adresse email du compte", example = "marie.lectrice@example.fr")
    @NotBlank(message = "L'email est obligatoire.")
    String email,

    @Schema(description = "Mot de passe du compte", example = "BookHub!2026a")
    @NotBlank(message = "Le mot de passe est obligatoire.")
    String password
) {
}
