package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Emprunts du lecteur connecté")
public record MyLoansResponse(

    @Schema(description = "Emprunts en cours (ACTIVE ou LATE)")
    List<LoanResponse> current,

    @Schema(description = "Historique des emprunts rendus")
    List<LoanResponse> history
) {

}
