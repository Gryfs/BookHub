package fr.eni.bookhub.dto;

import fr.eni.bookhub.domain.Loan;
import fr.eni.bookhub.domain.LoanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Emprunt vu par le bibliothécaire (lecteur identifié)")
public record LibrarianLoanResponse(

    @Schema(description = "Identifiant de l'emprunt")
    Long id,

    @Schema(description = "Identifiant du livre")
    Long bookId,

    @Schema(description = "Titre emprunté")
    String bookTitle,

    @Schema(description = "Auteur")
    String bookAuthor,

    @Schema(description = "Identifiant du lecteur")
    Long userId,

    @Schema(description = "E-mail du lecteur")
    String userEmail,

    @Schema(description = "Prénom du lecteur")
    String userFirstName,

    @Schema(description = "Nom du lecteur")
    String userLastName,

    LocalDate loanDate,

    LocalDate dueDate,

    LocalDate returnDate,

    LoanStatus status
) {

  public static LibrarianLoanResponse from(Loan loan) {
    return new LibrarianLoanResponse(
        loan.getId(),
        loan.getBook().getId(),
        loan.getBook().getTitle(),
        loan.getBook().getAuthor(),
        loan.getUser().getId(),
        loan.getUser().getEmail(),
        loan.getUser().getFirstName(),
        loan.getUser().getLastName(),
        loan.getLoanDate(),
        loan.getDueDate(),
        loan.getReturnDate(),
        loan.getStatus());
  }

}
