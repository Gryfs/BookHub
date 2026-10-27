package fr.eni.bookhub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "livre")
@Getter
@Setter
@NoArgsConstructor
public class Book {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_livre")
  private Long id;

  @Column(name = "isbn", nullable = false)
  private String isbn;

  @Column(name = "titre", nullable = false)
  private String title;

  @Column(name = "auteur", nullable = false)
  private String author;

  @Column(name = "description")
  private String description;

  @Column(name = "url_couverture")
  private String coverUrl;

  @Column(name = "annee_publication")
  private Short publicationYear;

  @Column(name = "nb_exemplaires_total", nullable = false)
  private Integer totalCopies = 1;

  @Column(name = "nb_exemplaires_disponibles", nullable = false, updatable = false)
  private Integer availableCopies;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "id_categorie", nullable = false)
  private Category category;

  @PrePersist
  private void initAvailableCopies() {
    if (availableCopies == null) {
      availableCopies = totalCopies;
    }
  }

}
