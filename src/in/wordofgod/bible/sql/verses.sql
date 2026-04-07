BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS verses (
	book_number NUMERIC NOT NULL, 
	chapter NUMERIC NOT NULL, 
	verse NUMERIC NOT NULL, 
	text TEXT NOT NULL DEFAULT '', 
	PRIMARY KEY (book_number, chapter, verse) );

CREATE UNIQUE INDEX verses_index on verses (book_number, chapter, verse);
COMMIT;
