package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "Format de toutes les erreurs de l'API (ProblemDetail, application/problem+json)")
public record ErrorResponse(

    @Schema(description = "Type d'erreur", example = "about:blank")
    String type,

    @Schema(description = "Titre court de l'erreur", example = "Données invalides")
    String title,

    @Schema(description = "Code HTTP", example = "400")
    int status,

    @Schema(description = "Message lisible, affichable à l'utilisateur",
        example = "Un ou plusieurs champs sont invalides.")
    String detail,

    @Schema(description = "Route appelée", example = "/api/auth/register")
    String instance,

    @Schema(description = "Erreurs par champ, présent uniquement pour les erreurs de validation (400)",
        example = "{\"password\": \"Le mot de passe doit contenir au moins 12 caractères...\"}")
    Map<String, String> errors
) {
}
