package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Nouvelles informations saisies sur la page Mon profil")
public record UpdateProfileRequest(

    @Schema(description = "Prénom, obligatoire", example = "Marie")
    @NotBlank(message = "Le prénom est obligatoire.")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères.")
    String firstName,

    @Schema(description = "Nom, obligatoire", example = "Dupont")
    @NotBlank(message = "Le nom est obligatoire.")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères.")
    String lastName,

    @Schema(description = "Téléphone français, facultatif. Vide pour le supprimer", example = "06 12 34 56 78")
    @Pattern(regexp = "^$|^(\\+33 ?|0)[1-9]([ .-]?[0-9]{2}){4}$",
        message = "Le numéro de téléphone n'est pas valide.")
    String phone,

    @Schema(description = "Nom affiché sur les avis (50 caractères maximum). Vide pour afficher le nom réel",
        example = "MarieLit")
    @Size(max = 50, message = "Le pseudonyme ne doit pas dépasser 50 caractères.")
    String pseudonym
) {
}
