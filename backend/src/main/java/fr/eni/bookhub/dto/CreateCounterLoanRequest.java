package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Emprunt enregistré au comptoir pour un lecteur")
public record CreateCounterLoanRequest(

    @Schema(description = "E-mail du lecteur", example = "marie.lectrice@example.fr")
    @NotBlank(message = "L'e-mail du lecteur est obligatoire")
    @Email(message = "L'e-mail du lecteur n'est pas valide")
    String userEmail,

    @Schema(description = "Identifiant du livre", example = "9")
    @NotNull(message = "Le livre est obligatoire")
    Long bookId
) {

}
