package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Informations saisies dans le formulaire d'inscription")
public record RegisterRequest(

    @Schema(description = "Prénom du lecteur", example = "Jean")
    @NotBlank(message = "Le prénom est obligatoire.")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères.")
    String firstName,

    @Schema(description = "Nom du lecteur", example = "Martin")
    @NotBlank(message = "Le nom est obligatoire.")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères.")
    String lastName,

    @Schema(description = "Adresse email, sert d'identifiant de connexion (unique)",
        example = "jean.martin@example.fr")
    @NotBlank(message = "L'email est obligatoire.")
    @Email(message = "L'email n'est pas valide.")
    @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères.")
    String email,

    @Schema(description = PasswordRules.MESSAGE, example = "MonMotDePasse!2026")
    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(max = 72, message = "Le mot de passe ne doit pas dépasser 72 caractères.")
    @Pattern(regexp = PasswordRules.REGEX, message = PasswordRules.MESSAGE)
    String password,

    @Schema(description = "Acceptation du traitement des données personnelles (RGPD), doit valoir true",
        example = "true")
    @NotNull(message = "Vous devez accepter le traitement de vos données.")
    @AssertTrue(message = "Vous devez accepter le traitement de vos données.")
    Boolean consent
) {
}
