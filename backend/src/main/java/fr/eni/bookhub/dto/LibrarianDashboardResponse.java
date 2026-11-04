package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Tableau de bord bibliothécaire (US-DASH-02)")
public record LibrarianDashboardResponse(
    LibraryStatsResponse stats,
    List<TopBookStatResponse> topBooks
) {}
