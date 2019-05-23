CREATE OR REPLACE PROCEDURE login(IN username varchar(25), IN passwd varchar(25))
BEGIN
	select name, password from users where name=username and password=passwd;
END;