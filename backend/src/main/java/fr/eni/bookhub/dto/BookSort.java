package fr.eni.bookhub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tri des résultats : TITLE (alphabétique), RATING (mieux notés d'abord), "
    + "RECENT (derniers ajoutés d'abord)")
public enum BookSort {
  TITLE,
  RATING,
  RECENT
}
