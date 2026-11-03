package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpsertRatingRequest(

    @Schema(description = "Identifiant du livre", example = "3")
    @NotNull(message = "Le livre est obligatoire")
    Long bookId,

    @Schema(description = "Note de 1 à 5", example = "4")
    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimum est 1")
    @Max(value = 5, message = "La note maximum est 5")
    Short score,

    @Schema(description = "Commentaire optionnel (1000 caractères max)")
    @Size(max = 1000, message = "Le commentaire ne peut pas dépasser 1000 caractères")
    String comment
) {

}
