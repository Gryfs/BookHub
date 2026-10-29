package fr.eni.bookhub.dto;

public final class PasswordRules {

  public static final String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{12,}$";

  public static final String MESSAGE = "Le mot de passe doit contenir au moins 12 caractères, "
      + "dont une majuscule, une minuscule, un chiffre et un caractère spécial.";

  private PasswordRules() {
  }

}
