package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Informations saisies dans le formulaire d'ajout ou de modification d'un livre")
public record BookRequest(

    @Schema(description = "Titre", example = "Germinal")
    @NotBlank(message = "Le titre est obligatoire.")
    @Size(max = 255, message = "Le titre ne doit pas dépasser 255 caractères.")
    String title,

    @Schema(description = "Auteur", example = "Émile Zola")
    @NotBlank(message = "L'auteur est obligatoire.")
    @Size(max = 255, message = "L'auteur ne doit pas dépasser 255 caractères.")
    String author,

    @Schema(description = "ISBN-13, avec ou sans tirets. Unique dans le catalogue", example = "978-2-07-041080-4")
    @NotBlank(message = "L'ISBN est obligatoire.")
    @Size(max = 17, message = "L'ISBN ne doit pas dépasser 17 caractères.")
    @Pattern(regexp = "^(?=(?:\\D*\\d){13}$)\\d+(-\\d+)*$",
        message = "L'ISBN doit contenir 13 chiffres, éventuellement séparés par des tirets.")
    String isbn,

    @Schema(description = "Identifiant de la catégorie (voir GET /api/categories)", example = "1")
    @NotNull(message = "La catégorie est obligatoire.")
    Integer categoryId,

    @Schema(description = "Résumé du livre, facultatif", example = "La vie des mineurs du Nord au XIXe siècle.")
    @Size(max = 2000, message = "La description ne doit pas dépasser 2000 caractères.")
    String description,

    @Schema(description = "Année de publication, facultative", example = "1885")
    @Min(value = 1450, message = "L'année de publication doit être comprise entre 1450 et 2100.")
    @Max(value = 2100, message = "L'année de publication doit être comprise entre 1450 et 2100.")
    Short publicationYear,

    @Schema(description = "Nombre total d'exemplaires. En modification, il ne peut pas être inférieur "
        + "au nombre d'exemplaires actuellement empruntés", example = "3")
    @NotNull(message = "Le nombre d'exemplaires est obligatoire.")
    @Min(value = 0, message = "Le nombre d'exemplaires ne peut pas être négatif.")
    @Max(value = 1000, message = "Le nombre d'exemplaires ne doit pas dépasser 1000.")
    Integer totalCopies,

    @Schema(description = "Adresse (http ou https) de l'image de couverture, facultative",
        example = "https://covers.openlibrary.org/b/isbn/9782070410804-L.jpg")
    @Size(max = 500, message = "L'adresse de la couverture ne doit pas dépasser 500 caractères.")
    @Pattern(regexp = "^$|^https?://\\S+$", message = "L'adresse de la couverture doit commencer par http:// ou https://.")
    String coverUrl
) {
}
