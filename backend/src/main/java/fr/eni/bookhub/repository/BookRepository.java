package fr.eni.bookhub.repository;

import fr.eni.bookhub.domain.Book;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

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

  @Query("select count(b) > 0 from Book b where replace(b.isbn, '-', '') = :digits")
  boolean existsByIsbnDigits(@Param("digits") String digits);

  @Query("select count(b) > 0 from Book b where replace(b.isbn, '-', '') = :digits and b.id <> :id")
  boolean existsByIsbnDigitsAndIdNot(@Param("digits") String digits, @Param("id") Long id);

}
