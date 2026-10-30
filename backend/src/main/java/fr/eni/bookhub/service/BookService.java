package fr.eni.bookhub.service;

import fr.eni.bookhub.domain.Book;
import fr.eni.bookhub.dto.BookSort;
import fr.eni.bookhub.dto.BookSummaryResponse;
import fr.eni.bookhub.dto.PageResponse;
import fr.eni.bookhub.dto.BookDetailResponse;
import fr.eni.bookhub.dto.BookReviewResponse;
import fr.eni.bookhub.repository.BookRepository;
import fr.eni.bookhub.repository.BookSpecifications;
import fr.eni.bookhub.repository.RatingRepository;
import fr.eni.bookhub.service.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

  private final BookRepository bookRepository;
  private final RatingRepository ratingRepository;

  @Transactional(readOnly = true)
  public PageResponse<BookSummaryResponse> getCatalogue(Pageable pageable) {
    return PageResponse.from(bookRepository.findAll(pageable), BookSummaryResponse::from);
  }

  @Transactional(readOnly = true)
  public PageResponse<BookSummaryResponse> search(String text, Integer categoryId, Boolean available,
      BookSort sort, int page, int size) {
    List<Specification<Book>> specifications = new ArrayList<>();
    specifications.add(BookSpecifications.matchesText(text));
    specifications.add(BookSpecifications.inCategory(categoryId));
    specifications.add(BookSpecifications.isAvailable(available));

    Pageable pageable;
    switch (sort) {
      case RATING -> {
        specifications.add(BookSpecifications.orderByAverageRating());
        pageable = PageRequest.of(page, size);
      }
      case RECENT -> pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
      default -> pageable = PageRequest.of(page, size, Sort.by("title"));
    }

    return PageResponse.from(bookRepository.findAll(Specification.allOf(specifications), pageable),
        BookSummaryResponse::from);
  }

  @Transactional(readOnly = true)
  public BookDetailResponse getBookDetail(long id) {
    Book book = bookRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Livre", id));
    Double average = ratingRepository.findAverageScoreByBookId(id);
    if (average != null) {
      average = Math.round(average * 10.0) / 10.0;
    }
    List<BookReviewResponse> reviews = ratingRepository.findByBookIdOrderByIdDesc(id).stream()
        .map(BookReviewResponse::from)
        .toList();
    return BookDetailResponse.from(book, average, reviews);
  }

}
