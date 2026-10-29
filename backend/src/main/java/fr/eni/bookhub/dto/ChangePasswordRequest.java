package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Mots de passe saisis sur la page Changer le mot de passe")
public record ChangePasswordRequest(

    @Schema(description = "Mot de passe actuel, pour confirmer l'identité", example = "BookHub!2026a")
    @NotBlank(message = "Le mot de passe actuel est obligatoire.")
    String currentPassword,

    @Schema(description = "Nouveau mot de passe, différent de l'actuel. " + PasswordRules.MESSAGE,
        example = "NouveauMotDePasse!2026")
    @NotBlank(message = "Le nouveau mot de passe est obligatoire.")
    @Size(max = 72, message = "Le mot de passe ne doit pas dépasser 72 caractères.")
    @Pattern(regexp = PasswordRules.REGEX, message = PasswordRules.MESSAGE)
    String newPassword
) {
}
