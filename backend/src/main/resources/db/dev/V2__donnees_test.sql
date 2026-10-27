/* ---------------------------------------------------------------------
   5. Données de test
   Mot de passe de tous les comptes : BookHub!2026a  (BCrypt, coût 12)
   --------------------------------------------------------------------- */
INSERT INTO utilisateur (email, mot_de_passe_hash, prenom, nom, telephone, pseudonyme, role)
SELECT v.email, '$2a$12$m1gQyIDNJJo25079xKwyDuG5En9S4muRE..nJatPHycsYCkeL5iuy',
       v.prenom, v.nom, v.telephone, v.pseudonyme, v.role
  FROM (VALUES
 ('sophie.admin@quartier-solidaire.fr', 'Sophie', 'Martin', '0601020304', NULL,       'ADMIN'),
 ('ahmed.biblio@quartier-solidaire.fr', 'Ahmed',  'Benali', '0605060708', NULL,       'LIBRARIAN'),
 ('marie.lectrice@example.fr',          'Marie',  'Dupont', '0611121314', 'MarieLit', 'USER'),
 ('lucas.lecteur@example.fr',           'Lucas',  'Moreau', NULL,         NULL,       'USER'),
 ('ines.lectrice@example.fr',           'Inès',   'Garcia', NULL,         'Ines_G',   'USER')
  ) AS v(email, prenom, nom, telephone, pseudonyme, role);

INSERT INTO categorie (libelle) VALUES
 ('Roman'), ('Science-fiction'), ('Policier'), ('Jeunesse'), ('Bande dessinée'), ('Essai');

INSERT INTO livre (isbn, titre, auteur, description, annee_publication, nb_exemplaires_total, nb_exemplaires_disponibles, id_categorie) VALUES
 ('978-2-07-036822-8', 'L''Étranger',                 'Albert Camus',      'Meursault, un employé de bureau à Alger...', 1942, 3, 3, 1),
 ('978-2-07-040850-4', 'Le Petit Prince',             'Antoine de Saint-Exupéry', 'Un aviateur rencontre un petit prince venu d''une autre planète.', 1943, 4, 4, 4),
 ('978-2-253-00447-9', 'Fondation',                   'Isaac Asimov',      'Le psychohistorien Hari Seldon prévoit la chute de l''Empire.', 1951, 2, 2, 2),
 ('978-2-266-11156-3', 'Dune',                        'Frank Herbert',     'Sur Arrakis, planète désertique, se joue l''avenir de l''Empire.', 1965, 1, 1, 2),
 ('978-2-253-17580-3', 'Le Crime de l''Orient-Express', 'Agatha Christie',  'Hercule Poirot enquête dans un train bloqué par la neige.', 1934, 2, 2, 3),
 ('978-2-203-00101-1', 'Tintin au Tibet',             'Hergé',             'Tintin part à la recherche de son ami Tchang.', 1960, 2, 2, 5),
 ('978-2-07-036024-6', 'Les Misérables',              'Victor Hugo',       'Le destin de Jean Valjean dans la France du XIXe siècle.', 1862, 2, 2, 1),
 ('978-2-08-121082-5', 'Sapiens',                     'Yuval Noah Harari', 'Une brève histoire de l''humanité.', 2011, 1, 1, 6),
 ('978-2-07-061275-8', '1984',                        'George Orwell',     'Winston Smith vit sous la surveillance de Big Brother.', 1949, 3, 3, 2),
 ('978-2-01-322384-6', 'Astérix le Gaulois',          'René Goscinny',     'Un village gaulois résiste encore et toujours à l''envahisseur.', 1961, 2, 2, 5),
 ('978-2-07-054127-0', 'Harry Potter à l''école des sorciers', 'J. K. Rowling', 'Harry découvre qu''il est un sorcier.', 1997, 3, 3, 4),
 ('978-2-253-15209-5', 'La Vérité sur l''affaire Harry Quebert', 'Joël Dicker', 'Un jeune écrivain enquête sur son mentor.', 2012, 1, 1, 3);

/* Emprunts en cours : T1 décrémente nb_exemplaires_disponibles */
INSERT INTO emprunt (id_utilisateur, id_livre, date_emprunt, statut) VALUES
 (3, 4, CURRENT_DATE -  5, 'ACTIVE'),   -- Marie : Dune (dernier exemplaire)
 (3, 9, CURRENT_DATE - 10, 'ACTIVE'),   -- Marie : 1984
 (4, 8, CURRENT_DATE - 20, 'LATE'),     -- Lucas : Sapiens, en retard de 6 jours
 (5, 1, CURRENT_DATE -  2, 'ACTIVE');   -- Inès : L'Étranger

/* Historique (rendus) : T1 ne touche pas au stock */
INSERT INTO emprunt (id_utilisateur, id_livre, date_emprunt, date_retour_effective, statut) VALUES
 (3, 2, CURRENT_DATE - 60, CURRENT_DATE - 50, 'RETURNED'),
 (4, 2, CURRENT_DATE - 40, CURRENT_DATE - 23, 'RETURNED'),   -- rendu avec 3 jours de retard
 (5, 5, CURRENT_DATE - 35, CURRENT_DATE - 25, 'RETURNED'),
 (3, 3, CURRENT_DATE - 90, CURRENT_DATE - 80, 'RETURNED');

INSERT INTO reservation (id_utilisateur, id_livre, date_reservation) VALUES
 (5, 8, now() - INTERVAL '5 days'),    -- Inès : rang 1 sur Sapiens (retenu par Lucas, en retard)
 (5, 4, now() - INTERVAL '30 hours'),  -- Inès : rang 1 sur Dune
 (4, 4, now() - INTERVAL '2 hours');   -- Lucas : rang 2 sur Dune

INSERT INTO noter (id_utilisateur, id_livre, note, commentaire) VALUES
 (3, 2, 5, 'Un classique à relire à tout âge.'),
 (4, 2, 4, 'Très poétique, idéal à lire avec les enfants.'),
 (5, 5, 4, 'Une intrigue redoutable, la fin surprend.'),
 (3, 3, 5, NULL);

