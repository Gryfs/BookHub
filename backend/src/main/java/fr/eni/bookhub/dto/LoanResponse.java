package fr.eni.bookhub.dto;

import fr.eni.bookhub.domain.Loan;
import fr.eni.bookhub.domain.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Emprunt d'un livre par un lecteur")
public record LoanResponse(

    @Schema(description = "Identifiant de l'emprunt", example = "15")
    Long id,

    @Schema(description = "Identifiant du livre", example = "9")
    Long bookId,

    @Schema(description = "Titre du livre emprunté", example = "1984")
    String bookTitle,

    @Schema(description = "Auteur", example = "George Orwell")
    String bookAuthor,

    @Schema(description = "Date de début d'emprunt")
    LocalDate loanDate,

    @Schema(description = "Date limite de retour (emprunt + 14 jours)")
    LocalDate dueDate,

    @Schema(description = "Date effective de retour, null si en cours")
    LocalDate returnDate,

    @Schema(description = "ACTIVE, LATE ou RETURNED")
    LoanStatus status
) {

  public static LoanResponse from(Loan loan) {
    return new LoanResponse(
        loan.getId(),
        loan.getBook().getId(),
        loan.getBook().getTitle(),
        loan.getBook().getAuthor(),
        loan.getLoanDate(),
        loan.getDueDate(),
        loan.getReturnDate(),
        loan.getStatus());
  }

}
