package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;

@Schema(description = "Une page de résultats")
public record PageResponse<T>(

    @Schema(description = "Éléments de la page")
    List<T> content,

    @Schema(description = "Numéro de la page, en commençant à 0", example = "0")
    int page,

    @Schema(description = "Nombre maximum d'éléments par page", example = "20")
    int size,

    @Schema(description = "Nombre total d'éléments", example = "12")
    long totalElements,

    @Schema(description = "Nombre total de pages", example = "1")
    int totalPages
) {

  public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
    return new PageResponse<>(page.getContent().stream().map(mapper).toList(), page.getNumber(),
        page.getSize(), page.getTotalElements(), page.getTotalPages());
  }

}
