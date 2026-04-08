BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "Details" ("Title" NVARCHAR(255), "Description" TEXT, "Abbreviation" NVARCHAR(50), "Comments" TEXT, "Version" TEXT, "VersionDate" DATETIME, "PublishDate" DATETIME, "Publisher" TEXT, "Author" TEXT, "Creator" TEXT, "Source" TEXT, "EditorialComments" TEXT, "Language" NVARCHAR(3), "RightToLeft" BOOL, "OT" BOOL, "NT" BOOL, "Strong" BOOL, "VerseRules" TEXT, "CustomCSS" TEXT);
COMMIT;
