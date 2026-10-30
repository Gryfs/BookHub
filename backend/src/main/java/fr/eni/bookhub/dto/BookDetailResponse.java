package fr.eni.bookhub.dto;

import fr.eni.bookhub.domain.Book;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Fiche détaillée d'un livre (page détail)")
public record BookDetailResponse(

    @Schema(description = "Identifiant", example = "4")
    Long id,

    @Schema(description = "ISBN", example = "978-2-07-036822-8")
    String isbn,

    @Schema(description = "Titre", example = "Dune")
    String title,

    @Schema(description = "Auteur", example = "Frank Herbert")
    String author,

    @Schema(description = "Résumé ou description")
    String description,

    @Schema(description = "URL de couverture")
    String coverUrl,

    @Schema(description = "Année de publication", example = "1965")
    Short publicationYear,

    @Schema(description = "Catégorie")
    CategoryResponse category,

    @Schema(description = "Nombre d'exemplaires disponibles à l'emprunt", example = "0")
    int availableCopies,

    @Schema(description = "Nombre total d'exemplaires", example = "2")
    int totalCopies,

    @Schema(description = "true si au moins un exemplaire est empruntable", example = "false")
    boolean available,

    @Schema(description = "Note moyenne sur 5, null si aucun avis")
    Double averageRating,

    @Schema(description = "Avis des lecteurs, du plus récent au plus ancien")
    List<BookReviewResponse> reviews
) {

  public static BookDetailResponse from(Book book, Double averageRating, List<BookReviewResponse> reviews) {
    return new BookDetailResponse(
        book.getId(),
        book.getIsbn(),
        book.getTitle(),
        book.getAuthor(),
        book.getDescription(),
        book.getCoverUrl(),
        book.getPublicationYear(),
        CategoryResponse.from(book.getCategory()),
        book.getAvailableCopies(),
        book.getTotalCopies(),
        book.getAvailableCopies() > 0,
        averageRating,
        reviews);
  }

}
