package fr.eni.bookhub.service;

import fr.eni.bookhub.dto.CategoryResponse;
import fr.eni.bookhub.repository.CategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

  private final CategoryRepository categoryRepository;

  @Transactional(readOnly = true)
  public List<CategoryResponse> getCategories() {
    return categoryRepository.findAllByOrderByLabelAsc().stream()
        .map(CategoryResponse::from)
        .toList();
  }

}
