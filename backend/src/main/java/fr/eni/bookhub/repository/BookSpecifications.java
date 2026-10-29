package fr.eni.bookhub.repository;

import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.domain.Rating;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.query.criteria.HibernateCriteriaBuilder;
import org.springframework.data.jpa.domain.Specification;

public final class BookSpecifications {

  private static final char ESCAPE = '\\';

  private BookSpecifications() {
  }

  public static Specification<Book> matchesText(String text) {
    if (text == null || text.isBlank()) {
      return Specification.unrestricted();
    }
    String pattern = "%" + escapeLike(text.trim()) + "%";
    String isbnDigits = text.replaceAll("[\\s-]", "");

    return (root, query, cb) -> {
      HibernateCriteriaBuilder hcb = (HibernateCriteriaBuilder) cb;
      List<Predicate> predicates = new ArrayList<>();
      predicates.add(hcb.ilike(root.get("title"), pattern, ESCAPE));
      predicates.add(hcb.ilike(root.get("author"), pattern, ESCAPE));
      predicates.add(hcb.ilike(root.get("isbn"), pattern, ESCAPE));

      if (isbnDigits.matches("\\d{3,}")) {
        Expression<String> isbnWithoutDashes = cb.function("replace", String.class, root.get("isbn"),
            cb.literal("-"), cb.literal(""));
        predicates.add(cb.like(isbnWithoutDashes, "%" + isbnDigits + "%"));
      }
      return cb.or(predicates.toArray(new Predicate[0]));
    };
  }

  public static Specification<Book> inCategory(Integer categoryId) {
    if (categoryId == null) {
      return Specification.unrestricted();
    }
    return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
  }

  public static Specification<Book> isAvailable(Boolean available) {
    if (available == null) {
      return Specification.unrestricted();
    }
    return (root, query, cb) -> available
        ? cb.greaterThan(root.get("availableCopies"), 0)
        : cb.equal(root.get("availableCopies"), 0);
  }

  public static Specification<Book> orderByAverageRating() {
    return (root, query, cb) -> {
      if (query.getResultType() != Long.class && query.getResultType() != long.class) {
        Subquery<Double> averageRating = query.subquery(Double.class);
        Root<Rating> rating = averageRating.from(Rating.class);
        averageRating.select(cb.avg(rating.get("score"))).where(cb.equal(rating.get("book"), root));
        query.orderBy(cb.desc(cb.coalesce(averageRating, 0.0)), cb.asc(root.get("title")));
      }
      return null;
    };
  }

  private static String escapeLike(String text) {
    return text.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
  }

}
