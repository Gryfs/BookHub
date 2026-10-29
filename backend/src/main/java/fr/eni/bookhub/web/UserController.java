package fr.eni.bookhub.web;

import fr.eni.bookhub.config.OpenApiConfig;
import fr.eni.bookhub.dto.ChangePasswordRequest;
import fr.eni.bookhub.dto.ErrorResponse;
import fr.eni.bookhub.dto.ProfileResponse;
import fr.eni.bookhub.dto.UpdateProfileRequest;
import fr.eni.bookhub.service.UserService;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
@Tag(name = "Profil",
    description = "Gestion du compte de l'utilisateur connecté (page Mon profil). Toutes ces routes "
        + "demandent un jeton JWT et agissent uniquement sur le compte du propriétaire du jeton.")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@ApiResponse(responseCode = "401", description = "Jeton absent, invalide ou expiré : il faut se reconnecter",
    content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
        examples = @ExampleObject(value = """
            {"type": "about:blank", "title": "Non authentifié", "status": 401,
             "detail": "Authentification requise.", "instance": "/api/users/me"}
            """)))
public class UserController {

  private final UserService userService;

  @GetMapping
  @Operation(summary = "Consulter ses informations personnelles",
      description = """
          Sert à l'affichage de la page **Mon profil** : renvoie les informations du compte connecté.

          - Accès : tout utilisateur connecté (USER, LIBRARIAN, ADMIN).
          - Contrairement à la réponse du login, contient aussi le téléphone et le pseudonyme : \
          ces données ne sont envoyées que sur cette page (minimisation RGPD).
          """)
  @ApiResponse(responseCode = "200", description = "Informations personnelles de l'utilisateur connecté")
  public ProfileResponse getProfile(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal) {
    return userService.getProfile(principal.getUsername());
  }

  @PutMapping
  @Operation(summary = "Modifier ses informations personnelles",
      description = """
          Sert au bouton **Enregistrer** de la page Mon profil.

          - Accès : tout utilisateur connecté.
          - Modifie le prénom, le nom, le téléphone et le pseudonyme.
          - Le pseudonyme est le nom affiché sur les avis : s'il est vide, c'est le nom réel qui est affiché.
          - Un téléphone ou un pseudonyme vide est supprimé de la base.
          - L'email et le rôle ne sont pas modifiables par cette route.
          """)
  @ApiResponse(responseCode = "200", description = "Profil mis à jour, renvoie les nouvelles informations")
  @ApiResponse(responseCode = "400", description = "Champs invalides : prénom ou nom vide, téléphone mal formé, pseudonyme trop long",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
          examples = @ExampleObject(value = """
              {"type": "about:blank", "title": "Données invalides", "status": 400,
               "detail": "Un ou plusieurs champs sont invalides.", "instance": "/api/users/me",
               "errors": {"phone": "Le numéro de téléphone n'est pas valide."}}
              """)))
  public ProfileResponse updateProfile(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
      @Valid @RequestBody UpdateProfileRequest request) {
    return userService.updateProfile(principal.getUsername(), request);
  }

  @PutMapping("/password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Changer son mot de passe",
      description = """
          Sert à la page **Changer le mot de passe**.

          - Accès : tout utilisateur connecté.
          - Le mot de passe actuel est demandé pour vérifier que c'est bien le titulaire du compte.
          - Le nouveau mot de passe suit la même politique qu'à l'inscription \
          (12 caractères minimum, une majuscule, une minuscule, un chiffre, un caractère spécial) \
          et doit être différent de l'ancien.
          - Le jeton actuel reste valable jusqu'à son expiration.
          """)
  @ApiResponse(responseCode = "204", description = "Mot de passe modifié")
  @ApiResponse(responseCode = "400", description = "Mot de passe actuel incorrect, nouveau mot de passe trop faible ou identique à l'ancien",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
          examples = @ExampleObject(value = """
              {"type": "about:blank", "title": "Données invalides", "status": 400,
               "detail": "Un ou plusieurs champs sont invalides.", "instance": "/api/users/me/password",
               "errors": {"currentPassword": "Le mot de passe actuel est incorrect."}}
              """)))
  public void changePassword(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal,
      @Valid @RequestBody ChangePasswordRequest request) {
    userService.changePassword(principal.getUsername(), request);
  }

  @DeleteMapping
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @Operation(summary = "Supprimer son compte (droit à l'oubli RGPD)",
      description = """
          Sert à la page **Supprimer mon compte**.

          - Accès : tout utilisateur connecté, uniquement pour son propre compte.
          - Refusé tant qu'un emprunt est en cours (statut ACTIVE ou LATE) : le livre doit d'abord être rendu.
          - Les réservations et les avis de l'utilisateur sont supprimés.
          - Le compte est anonymisé (nom, téléphone et email effacés, mot de passe inutilisable) : \
          il n'est plus possible de s'y connecter.
          - L'historique des emprunts est conservé de façon anonyme pour les statistiques de la bibliothèque.
          """)
  @ApiResponse(responseCode = "204", description = "Compte supprimé et anonymisé")
  @ApiResponse(responseCode = "409", description = "Suppression refusée : un emprunt est en cours",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
          examples = @ExampleObject(value = """
              {"type": "about:blank", "title": "Règle de gestion non respectée", "status": 409,
               "detail": "Vous ne pouvez pas supprimer votre compte tant qu'un emprunt est en cours.",
               "instance": "/api/users/me"}
              """)))
  public void deleteAccount(@Parameter(hidden = true) @AuthenticationPrincipal UserDetails principal) {
    userService.deleteAccount(principal.getUsername());
  }

}
