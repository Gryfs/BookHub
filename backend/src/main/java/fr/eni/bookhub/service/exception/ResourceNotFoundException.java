package fr.eni.bookhub.service.exception;

public class ResourceNotFoundException extends RuntimeException {

  public ResourceNotFoundException(String message) {
    super(message);
  }

  public ResourceNotFoundException(String ressource, Object id) {
    this(ressource + " introuvable (id " + id + ")");
  }

}
