package fr.eni.bookhub.web;

import fr.eni.bookhub.config.OpenApiConfig;
import fr.eni.bookhub.dto.CreateReservationRequest;
import fr.eni.bookhub.dto.ErrorResponse;
import fr.eni.bookhub.dto.ReservationResponse;
import fr.eni.bookhub.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Réservations", description = "File d'attente pour les ouvrages indisponibles (US-RESA-01/02).")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class ReservationController {

  private final ReservationService reservationService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Réserver un livre indisponible (US-RESA-01)")
  @ApiResponse(responseCode = "201", description = "Réservation créée")
  @ApiResponse(responseCode = "409", description = "Règle métier non respectée",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public ReservationResponse create(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
      @Valid @RequestBody CreateReservationRequest request) {
    return reservationService.create(principal.getUsername(), request.bookId());
  }

  @GetMapping("/my")
  @Operation(summary = "Mes réservations (US-RESA-02)")
  public List<ReservationResponse> myReservations(
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal) {
    return reservationService.listMine(principal.getUsername());
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Annuler une réservation")
  public void cancel(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
      @PathVariable Long id) {
    reservationService.cancel(principal.getUsername(), id);
  }

}
