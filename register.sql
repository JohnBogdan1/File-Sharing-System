CREATE OR REPLACE PROCEDURE register(IN username varchar(25), IN passwd varchar(25))
BEGIN
	insert into users (name, password) values(username, passwd);
END;