package fr.eni.bookhub.web;

import fr.eni.bookhub.config.OpenApiConfig;
import fr.eni.bookhub.dto.AdminDashboardResponse;
import fr.eni.bookhub.dto.ErrorResponse;
import fr.eni.bookhub.dto.UpdateUserRoleRequest;
import fr.eni.bookhub.dto.UserAdminResponse;
import fr.eni.bookhub.service.DashboardService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Administration", description = "US-DASH-03")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class AdminController {

  private final DashboardService dashboardService;

  @GetMapping("/dashboard")
  @Operation(summary = "Indicateurs administration (US-DASH-03)")
  public AdminDashboardResponse dashboard() {
    return dashboardService.adminDashboard();
  }

  @GetMapping("/users")
  @Operation(summary = "Liste des utilisateurs actifs")
  public List<UserAdminResponse> users() {
    return dashboardService.listUsersForAdmin();
  }

  @PutMapping("/users/{id}/role")
  @Operation(summary = "Modifier le rôle d'un utilisateur")
  @ApiResponse(responseCode = "200", description = "Rôle mis à jour")
  @ApiResponse(responseCode = "409", description = "Règle métier (dernier admin, etc.)",
      content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
  public UserAdminResponse updateRole(
      @Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
      @PathVariable Long id,
      @Valid @RequestBody UpdateUserRoleRequest request) {
    return dashboardService.updateUserRole(principal.getUsername(), id, request.role());
  }

}
