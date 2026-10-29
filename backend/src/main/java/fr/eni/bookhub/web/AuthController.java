package fr.eni.bookhub.web;

import fr.eni.bookhub.dto.ErrorResponse;
import fr.eni.bookhub.dto.LoginRequest;
import fr.eni.bookhub.dto.LoginResponse;
import fr.eni.bookhub.dto.RegisterRequest;
import fr.eni.bookhub.dto.UserResponse;
import fr.eni.bookhub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification",
    description = "Création de compte et connexion. Ces routes sont publiques : elles servent justement "
        + "à obtenir le jeton JWT nécessaire aux autres routes.")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Créer un compte lecteur",
      description = """
          Sert à la page **Inscription** : un visiteur crée son compte pour pouvoir emprunter, \
          réserver et noter des livres.

          - Accès : public, aucun jeton nécessaire.
          - Le compte reçoit toujours le rôle **USER** (les rôles LIBRARIAN et ADMIN ne s'obtiennent pas par cette route).
          - L'email est enregistré en minuscules et doit être unique.
          - Le mot de passe est haché en BCrypt (coût 12) avant d'être enregistré.
          - Le consentement RGPD est obligatoire : sans lui, le compte n'est pas créé.
          - Aucun jeton n'est renvoyé : l'utilisateur doit ensuite se connecter avec `POST /api/auth/login`.
          """)
  @ApiResponse(responseCode = "201", description = "Compte créé, renvoie les informations du nouveau compte")
  @ApiResponse(responseCode = "400", description = "Champs invalides : email mal formé, mot de passe trop faible, consentement absent... Le détail par champ est dans `errors`",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
          examples = @ExampleObject(value = """
              {"type": "about:blank", "title": "Données invalides", "status": 400,
               "detail": "Un ou plusieurs champs sont invalides.", "instance": "/api/auth/register",
               "errors": {"password": "Le mot de passe doit contenir au moins 12 caractères, dont une majuscule, une minuscule, un chiffre et un caractère spécial.",
                          "consent": "Vous devez accepter le traitement de vos données."}}
              """)))
  @ApiResponse(responseCode = "409", description = "Un compte existe déjà avec cette adresse email",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
          examples = @ExampleObject(value = """
              {"type": "about:blank", "title": "Règle de gestion non respectée", "status": 409,
               "detail": "Cette adresse email est déjà utilisée.", "instance": "/api/auth/register"}
              """)))
  public UserResponse register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/login")
  @Operation(summary = "Se connecter et obtenir un jeton JWT",
      description = """
          Sert à la page **Connexion** : vérifie l'email et le mot de passe puis renvoie un jeton JWT.

          - Accès : public, aucun jeton nécessaire.
          - Le jeton est signé en HS256, contient l'email et le rôle, et reste valable **24 h**.
          - Il doit être envoyé dans l'en-tête `Authorization: Bearer <jeton>` pour appeler les routes protégées \
          (dans Swagger : bouton **Authorize**).
          - Les informations renvoyées sur l'utilisateur permettent au front d'afficher son nom et de le \
          rediriger selon son rôle.
          - En cas d'échec, le message est volontairement le même, que l'email existe ou non.
          """)
  @ApiResponse(responseCode = "200", description = "Connexion réussie, renvoie le jeton et l'utilisateur")
  @ApiResponse(responseCode = "400", description = "Email ou mot de passe non renseigné",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
          examples = @ExampleObject(value = """
              {"type": "about:blank", "title": "Données invalides", "status": 400,
               "detail": "Un ou plusieurs champs sont invalides.", "instance": "/api/auth/login",
               "errors": {"password": "Le mot de passe est obligatoire."}}
              """)))
  @ApiResponse(responseCode = "401", description = "Email ou mot de passe incorrect (message générique)",
      content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ErrorResponse.class),
          examples = @ExampleObject(value = """
              {"type": "about:blank", "title": "Connexion refusée", "status": 401,
               "detail": "Email ou mot de passe incorrect.", "instance": "/api/auth/login"}
              """)))
  public LoginResponse login(@Valid @RequestBody LoginRequest request) {
    return authService.login(request);
  }

}
