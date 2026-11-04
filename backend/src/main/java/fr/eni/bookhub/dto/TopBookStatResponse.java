package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Livre parmi les plus empruntés (fn_top_livres)")
public record TopBookStatResponse(
    Long bookId,
    String title,
    String author,
    long loanCount
) {}
