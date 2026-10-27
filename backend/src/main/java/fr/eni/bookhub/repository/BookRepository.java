package fr.eni.bookhub.repository;

import fr.eni.bookhub.domain.Book;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

  @Override
  @EntityGraph(attributePaths = "category")
  Page<Book> findAll(Pageable pageable);

  @EntityGraph(attributePaths = "category")
  Optional<Book> findWithCategoryById(Long id);

  @EntityGraph(attributePaths = "category")
  Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author,
      Pageable pageable);

  @EntityGraph(attributePaths = "category")
  Page<Book> findByCategoryId(Integer categoryId, Pageable pageable);

  boolean existsByIsbn(String isbn);

}
