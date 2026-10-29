package fr.eni.bookhub.dto;

import fr.eni.bookhub.domain.Category;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Catégorie de livres, proposée dans le filtre de recherche")
public record CategoryResponse(

    @Schema(description = "Identifiant de la catégorie, à passer dans le filtre categoryId", example = "2")
    Integer id,

    @Schema(description = "Nom de la catégorie", example = "Science-fiction")
    String label
) {

  public static CategoryResponse from(Category category) {
    return new CategoryResponse(category.getId(), category.getLabel());
  }

}
