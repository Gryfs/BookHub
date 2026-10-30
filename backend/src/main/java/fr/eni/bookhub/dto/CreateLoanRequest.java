package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Demande d'emprunt d'un livre")
public record CreateLoanRequest(

    @Schema(description = "Identifiant du livre à emprunter", example = "9")
    @NotNull(message = "Le livre est obligatoire")
    Long bookId
) {

}
