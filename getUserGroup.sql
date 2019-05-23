CREATE OR REPLACE PROCEDURE getUserGroup(IN username varchar(25))
BEGIN
	select groupName from groups where owner=username;
END;