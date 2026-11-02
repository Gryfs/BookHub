package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CreateReservationRequest(

    @Schema(description = "Identifiant du livre indisponible", example = "4")
    @NotNull(message = "Le livre est obligatoire")
    Long bookId
) {

}
