package fr.eni.bookhub.web;

import fr.eni.bookhub.config.OpenApiConfig;
import fr.eni.bookhub.dto.ReaderDashboardResponse;
import fr.eni.bookhub.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Tableau de bord lecteur", description = "US-DASH-01")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class DashboardController {

  private final DashboardService dashboardService;

  @GetMapping("/reader")
  @PreAuthorize("hasRole('USER')")
  @Operation(summary = "Tableau de bord lecteur (US-DASH-01)")
  public ReaderDashboardResponse reader(
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal) {
    return dashboardService.readerDashboard(principal.getUsername());
  }

}
