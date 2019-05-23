CREATE OR REPLACE PROCEDURE findFileAtUsers(IN filename varchar(25))
BEGIN
	select * from files where name=filename;
END;