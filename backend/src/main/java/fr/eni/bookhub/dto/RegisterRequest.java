package fr.eni.bookhub.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

    @NotBlank(message = "Le prénom est obligatoire.")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères.")
    String firstName,

    @NotBlank(message = "Le nom est obligatoire.")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères.")
    String lastName,

    @NotBlank(message = "L'email est obligatoire.")
    @Email(message = "L'email n'est pas valide.")
    @Size(max = 255, message = "L'email ne doit pas dépasser 255 caractères.")
    String email,

    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(max = 72, message = "Le mot de passe ne doit pas dépasser 72 caractères.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d]).{12,}$",
        message = "Le mot de passe doit contenir au moins 12 caractères, dont une majuscule, une minuscule, un chiffre et un caractère spécial.")
    String password,

    @NotNull(message = "Vous devez accepter le traitement de vos données.")
    @AssertTrue(message = "Vous devez accepter le traitement de vos données.")
    Boolean consent
) {
}
