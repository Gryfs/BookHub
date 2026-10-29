package fr.eni.bookhub.dto;

import fr.eni.bookhub.domain.Book;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Livre tel qu'affiché dans la grille du catalogue (version allégée)")
public record BookSummaryResponse(

    @Schema(description = "Identifiant du livre, sert à ouvrir sa fiche détail", example = "4")
    Long id,

    @Schema(description = "Titre", example = "Dune")
    String title,

    @Schema(description = "Auteur", example = "Frank Herbert")
    String author,

    @Schema(description = "Adresse de l'image de couverture, null s'il n'y en a pas")
    String coverUrl,

    @Schema(description = "true si au moins un exemplaire peut être emprunté", example = "false")
    boolean available
) {

  public static BookSummaryResponse from(Book book) {
    return new BookSummaryResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getCoverUrl(),
        book.getAvailableCopies() > 0);
  }

}
