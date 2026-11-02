package fr.eni.bookhub.dto;

import fr.eni.bookhub.domain.Reservation;
import fr.eni.bookhub.domain.ReservationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Réservation d'un exemplaire indisponible")
public record ReservationResponse(

    Long id,

    Long bookId,

    String bookTitle,

    String bookAuthor,

    ReservationStatus status,

    OffsetDateTime reservationDate,

    @Schema(description = "Date limite de retrait (48 h), si la réservation est prête")
    OffsetDateTime pickupDeadline,

    @Schema(description = "Position dans la file (1 = prochain), si en attente")
    Integer queuePosition
) {

  public static ReservationResponse from(Reservation reservation, Integer queuePosition) {
    return new ReservationResponse(
        reservation.getId(),
        reservation.getBook().getId(),
        reservation.getBook().getTitle(),
        reservation.getBook().getAuthor(),
        reservation.getStatus(),
        reservation.getReservationDate(),
        reservation.getPickupDeadline(),
        queuePosition);
  }

}
