package fr.eni.bookhub.service.exception;

/**
 * Règle de gestion non respectée (3 emprunts maximum, retard en cours...) :
 * renvoyée en 409 Conflict avec le message destiné à l'utilisateur.
 */
public class BusinessRuleException extends RuntimeException {

  public BusinessRuleException(String message) {
    super(message);
  }

}
