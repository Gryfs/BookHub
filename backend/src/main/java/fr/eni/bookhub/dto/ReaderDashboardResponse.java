package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Tableau de bord lecteur (US-DASH-01)")
public record ReaderDashboardResponse(
    List<LoanResponse> currentLoans,
    List<ReservationResponse> reservations,
    boolean hasLateLoan,
    List<LoanResponse> recentlyReturned
) {}
