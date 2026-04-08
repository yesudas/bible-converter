BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "Bible" ("Book" INT,"Chapter" INT,"Verse" INT,"Scripture" TEXT);
CREATE UNIQUE INDEX "bible_key" ON "Bible" ("Book" ASC, "Chapter" ASC, "Verse" ASC);
COMMIT;
