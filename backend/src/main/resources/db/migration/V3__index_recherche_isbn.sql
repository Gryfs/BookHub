/* Recherche ILIKE '%...%' sur l'ISBN (US-BOOK-03) : complète les index
   trigrammes déjà posés sur le titre et l'auteur, pour que la recherche
   titre / auteur / ISBN reste entièrement indexée. */
CREATE INDEX ix_livre_isbn_trgm ON livre USING gin (isbn gin_trgm_ops);
