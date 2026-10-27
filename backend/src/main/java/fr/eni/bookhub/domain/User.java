package fr.eni.bookhub.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "utilisateur")
@Getter
@Setter
@NoArgsConstructor
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_utilisateur")
  private Long id;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "mot_de_passe_hash", nullable = false)
  private String passwordHash;

  @Column(name = "prenom", nullable = false)
  private String firstName;

  @Column(name = "nom", nullable = false)
  private String lastName;

  @Column(name = "telephone")
  private String phone;

  @Column(name = "pseudonyme")
  private String pseudonym;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Role role = Role.USER;

}
