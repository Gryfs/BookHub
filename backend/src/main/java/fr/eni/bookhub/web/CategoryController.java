package fr.eni.bookhub.web;

import fr.eni.bookhub.dto.CategoryResponse;
import fr.eni.bookhub.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Catalogue")
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  @Operation(summary = "Lister les catégories",
      description = """
          Sert à remplir la liste déroulante **Catégorie** de la page Recherche.

          - Accès : public, aucun jeton nécessaire.
          - Catégories triées par ordre alphabétique.
          - L'identifiant renvoyé s'utilise dans le paramètre `categoryId` de `GET /api/books/search`.
          """)
  @ApiResponse(responseCode = "200", description = "Liste des catégories")
  public List<CategoryResponse> getCategories() {
    return categoryService.getCategories();
  }

}
