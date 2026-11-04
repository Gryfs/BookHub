package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Indicateurs globaux de la bibliothèque (fn_stats_bibliotheque)")
public record LibraryStatsResponse(
    long bookCount,
    long copyCount,
    long activeLoans,
    long lateLoans,
    long waitingReservations
) {}
