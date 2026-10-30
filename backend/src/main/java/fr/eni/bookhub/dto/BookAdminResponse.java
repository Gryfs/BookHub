package fr.eni.bookhub.dto;

import fr.eni.bookhub.domain.Book;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ligne du tableau de gestion du catalogue (bibliothécaire)")
public record BookAdminResponse(

    @Schema(description = "Identifiant du livre", example = "2")
    Long id,

    @Schema(description = "ISBN", example = "978-2-07-040850-4")
    String isbn,

    @Schema(description = "Titre", example = "Le Petit Prince")
    String title,

    @Schema(description = "Auteur", example = "Antoine de Saint-Exupéry")
    String author,

    @Schema(description = "Nombre total d'exemplaires", example = "4")
    int totalCopies,

    @Schema(description = "Exemplaires disponibles à l'emprunt", example = "4")
    int availableCopies
) {

  public static BookAdminResponse from(Book book) {
    return new BookAdminResponse(book.getId(), book.getIsbn(), book.getTitle(), book.getAuthor(),
        book.getTotalCopies(), book.getAvailableCopies());
  }

}
