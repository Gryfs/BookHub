package fr.eni.bookhub.web.error;

import fr.eni.bookhub.service.exception.BusinessRuleException;
import fr.eni.bookhub.service.exception.InvalidFieldException;
import fr.eni.bookhub.service.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
      HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    Map<String, String> errors = new LinkedHashMap<>();
    ex.getBindingResult().getFieldErrors()
        .forEach(e -> errors.putIfAbsent(e.getField(), e.getDefaultMessage()));
    ex.getBindingResult().getGlobalErrors()
        .forEach(e -> errors.putIfAbsent(e.getObjectName(), e.getDefaultMessage()));

    ProblemDetail problem = ex.getBody();
    problem.setTitle("Données invalides");
    problem.setDetail("Un ou plusieurs champs sont invalides.");
    problem.setProperty("errors", errors);
    return handleExceptionInternal(ex, problem, headers, status, request);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
    Map<String, String> errors = new LinkedHashMap<>();
    ex.getConstraintViolations()
        .forEach(v -> errors.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
    ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Données invalides",
        "Un ou plusieurs paramètres sont invalides.");
    problem.setProperty("errors", errors);
    return problem;
  }

  @ExceptionHandler(InvalidFieldException.class)
  public ProblemDetail handleInvalidField(InvalidFieldException ex) {
    ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Données invalides",
        "Un ou plusieurs champs sont invalides.");
    problem.setProperty("errors", Map.of(ex.getField(), ex.getMessage()));
    return problem;
  }

  @ExceptionHandler(PropertyReferenceException.class)
  public ProblemDetail handleUnknownSortProperty(PropertyReferenceException ex) {
    return problem(HttpStatus.BAD_REQUEST, "Paramètre invalide",
        "Tri impossible sur le champ « " + ex.getPropertyName() + " ».");
  }

  @ExceptionHandler(BadCredentialsException.class)
  public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
    return problem(HttpStatus.UNAUTHORIZED, "Connexion refusée", "Email ou mot de passe incorrect.");
  }

  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleAuthentication(AuthenticationException ex) {
    return problem(HttpStatus.UNAUTHORIZED, "Non authentifié", "Authentification requise.");
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
    return problem(HttpStatus.FORBIDDEN, "Accès refusé",
        "Vous n'avez pas les droits nécessaires pour cette action.");
  }

  @ExceptionHandler(ResourceNotFoundException.class)
  public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
    return problem(HttpStatus.NOT_FOUND, "Ressource introuvable", ex.getMessage());
  }

  @ExceptionHandler(BusinessRuleException.class)
  public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
    return problem(HttpStatus.CONFLICT, "Règle de gestion non respectée", ex.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
    log.warn("Contrainte d'intégrité violée : {}", ex.getMostSpecificCause().getMessage());
    return problem(HttpStatus.CONFLICT, "Conflit de données",
        "L'opération est incompatible avec les données existantes.");
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleUnexpected(Exception ex) {
    log.error("Erreur inattendue", ex);
    return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne",
        "Une erreur inattendue est survenue.");
  }

  private static ProblemDetail problem(HttpStatus status, String title, String detail) {
    ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
    problem.setTitle(title);
    return problem;
  }

}
