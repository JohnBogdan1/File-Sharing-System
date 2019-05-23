CREATE OR REPLACE PROCEDURE checkDatabase(IN username varchar(25))
BEGIN
	select name from users where name=username;
END;