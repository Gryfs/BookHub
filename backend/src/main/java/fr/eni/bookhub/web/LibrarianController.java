package fr.eni.bookhub.web;

import fr.eni.bookhub.config.OpenApiConfig;
import fr.eni.bookhub.dto.CreateCounterLoanRequest;
import fr.eni.bookhub.dto.ErrorResponse;
import fr.eni.bookhub.dto.LibrarianLoanResponse;
import fr.eni.bookhub.dto.LoanResponse;
import fr.eni.bookhub.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/librarian")
@RequiredArgsConstructor
@PreAuthorize("hasRole('LIBRARIAN')")
@Tag(name = "Bibliothécaire", description = "Retours, retards et comptoir (US-LOAN-03 à 05).")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class LibrarianController {

  private final LoanService loanService;

  @GetMapping("/access-check")
  @Operation(summary = "Vérifier l'accès bibliothécaire")
  public String accessCheck() {
    return "ok";
  }

  @GetMapping("/loans/outstanding")
  @Operation(summary = "Emprunts à rendre",
      description = "Liste les emprunts ACTIVE ou LATE, triés par date limite de retour (US-LOAN-03).")
  public List<LibrarianLoanResponse> outstandingLoans() {
    return loanService.listOutstandingLoans();
  }

  @GetMapping("/loans/overdue")
  @Operation(summary = "Emprunts en retard",
      description = "Liste les emprunts LATE après passage de fn_marquer_retards (US-LOAN-04).")
  public List<LibrarianLoanResponse> overdueLoans() {
    return loanService.listOverdueLoans();
  }

  @PostMapping("/loans/{loanId}/return")
  @Operation(summary = "Enregistrer un retour",
      description = "Clôt l'emprunt et ré-incrémente le stock via le trigger emprunt_au_retour (US-LOAN-03).")
  @ApiResponse(responseCode = "200", description = "Retour enregistré")
  @ApiResponse(responseCode = "409", description = "Emprunt déjà rendu",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public LibrarianLoanResponse registerReturn(@PathVariable Long loanId) {
    return loanService.registerReturn(loanId);
  }

  @PostMapping("/loans/counter")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Emprunt au comptoir",
      description = """
          Crée un emprunt pour le lecteur identifié par e-mail (US-LOAN-05).
          Mêmes règles que l'emprunt en ligne : quota 3, pas de retard, exemplaire disponible.
          """)
  @ApiResponse(responseCode = "201", description = "Emprunt créé")
  @ApiResponse(responseCode = "409", description = "Règle métier non respectée",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public LoanResponse counterLoan(@Valid @RequestBody CreateCounterLoanRequest request) {
    return loanService.borrowAtCounter(request.userEmail(), request.bookId());
  }

}
