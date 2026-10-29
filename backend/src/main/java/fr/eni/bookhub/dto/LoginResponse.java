package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Résultat d'une connexion réussie")
public record LoginResponse(

    @Schema(description = "Jeton JWT à envoyer dans l'en-tête Authorization: Bearer, valable 24 h")
    String token,

    @Schema(description = "Utilisateur connecté, pour afficher son nom et choisir sa page d'accueil")
    UserResponse user
) {
}
