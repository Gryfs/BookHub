package fr.eni.bookhub.web;

import fr.eni.bookhub.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/librarian")
@Tag(name = "Bibliothécaire", description = "Routes réservées au rôle LIBRARIAN (RBAC).")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
public class LibrarianController {

  @GetMapping("/access-check")
  @PreAuthorize("hasRole('LIBRARIAN')")
  @Operation(summary = "Vérifier l'accès bibliothécaire", description = "Endpoint technique pour valider le RBAC (US-AUTH-05).")
  public String accessCheck() {
    return "ok";
  }

}
