package fr.eni.bookhub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "emprunt")
@Getter
@Setter
@NoArgsConstructor
public class Loan {

  public static final int LOAN_DURATION_DAYS = 14;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_emprunt")
  private Long id;

  @Column(name = "date_emprunt", nullable = false)
  private LocalDate loanDate = LocalDate.now();

  @Column(name = "date_retour_effective")
  private LocalDate returnDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "statut", nullable = false)
  private LoanStatus status = LoanStatus.ACTIVE;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_utilisateur", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_livre", nullable = false)
  private Book book;

  public LocalDate getDueDate() {
    return loanDate.plusDays(LOAN_DURATION_DAYS);
  }

}
