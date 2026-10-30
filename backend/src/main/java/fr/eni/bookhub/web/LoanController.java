package fr.eni.bookhub.web;

import fr.eni.bookhub.config.OpenApiConfig;
import fr.eni.bookhub.dto.CreateLoanRequest;
import fr.eni.bookhub.dto.ErrorResponse;
import fr.eni.bookhub.dto.LoanResponse;
import fr.eni.bookhub.dto.MyLoansResponse;
import fr.eni.bookhub.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@Tag(name = "Emprunts", description = "Emprunt et suivi personnel (lecteur connecté).")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class LoanController {

  private final LoanService loanService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Emprunter un livre",
      description = """
          Crée un emprunt pour le lecteur connecté (US-LOAN-01).

          - Maximum **3** emprunts en cours (ACTIVE ou LATE).
          - Durée **14 jours** (date de retour = date d'emprunt + 14 j).
          - Refusé si un emprunt est **en retard** (RG-LOAN-03).
          - Refusé si le livre n'a plus d'exemplaire disponible.
          - Un même livre ne peut pas être emprunté deux fois tant qu'il n'est pas rendu.
          """)
  @ApiResponse(responseCode = "201", description = "Emprunt créé")
  @ApiResponse(responseCode = "409", description = "Règle de gestion non respectée (quota, retard, indisponible...)",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class)))
  public LoanResponse borrow(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
      @Valid @RequestBody CreateLoanRequest request) {
    return loanService.borrow(principal.getUsername(), request.bookId());
  }

  @GetMapping("/my")
  @Operation(summary = "Lister mes emprunts",
      description = "Sert à la page **Mes emprunts** (US-LOAN-02) : onglets en cours et historique.")
  @ApiResponse(responseCode = "200", description = "Listes current et history")
  public MyLoansResponse getMyLoans(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal) {
    return loanService.getMyLoans(principal.getUsername());
  }

}
