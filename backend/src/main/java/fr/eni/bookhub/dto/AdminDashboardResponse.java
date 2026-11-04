package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Vue d'ensemble administration (US-DASH-03)")
public record AdminDashboardResponse(
    LibraryStatsResponse stats,
    List<TopBookStatResponse> topBooks,
    long userCount
) {}
