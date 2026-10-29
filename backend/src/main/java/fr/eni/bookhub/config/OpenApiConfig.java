package fr.eni.bookhub.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
    title = "BookHub API",
    version = "1.0",
    description = "API de la bibliothèque du Quartier Solidaire : authentification, profil, "
        + "catalogue, emprunts, réservations et avis. Les erreurs sont renvoyées au format "
        + "ProblemDetail (application/problem+json)."))
@SecurityScheme(
    name = OpenApiConfig.BEARER_AUTH,
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Jeton obtenu avec POST /api/auth/login, valable 24 h.")
public class OpenApiConfig {

  public static final String BEARER_AUTH = "bearerAuth";

}
