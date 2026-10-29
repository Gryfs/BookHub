package fr.eni.bookhub.service;

import fr.eni.bookhub.dto.BookSummaryResponse;
import fr.eni.bookhub.dto.PageResponse;
import fr.eni.bookhub.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookService {

  private final BookRepository bookRepository;

  @Transactional(readOnly = true)
  public PageResponse<BookSummaryResponse> getCatalogue(Pageable pageable) {
    return PageResponse.from(bookRepository.findAll(pageable), BookSummaryResponse::from);
  }

}
