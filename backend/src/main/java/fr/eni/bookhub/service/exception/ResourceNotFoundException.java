package fr.eni.bookhub.service.exception;

/** Ressource introuvable (livre, emprunt...) : renvoyée en 404 Not Found. */
public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }

  public ResourceNotFoundException(String ressource, Object id) {
    this(ressource + " introuvable (id " + id + ")");
  }

}
