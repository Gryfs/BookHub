package fr.eni.bookhub.dto;

import fr.eni.bookhub.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Changement de rôle utilisateur (US-DASH-03)")
public record UpdateUserRoleRequest(

    @NotNull
    @Schema(description = "USER, LIBRARIAN ou ADMIN")
    Role role
) {}
