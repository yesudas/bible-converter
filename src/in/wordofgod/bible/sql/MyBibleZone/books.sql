BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "books" (
	"book_number"	NUMERIC NOT NULL,
	"short_name"	TEXT NOT NULL,
	"long_name"	TEXT NOT NULL,
	"book_color"	TEXT NOT NULL,
	"sorting_order"	NUMERIC NOT NULL DEFAULT 0,
	PRIMARY KEY("book_number")
);
COMMIT;
