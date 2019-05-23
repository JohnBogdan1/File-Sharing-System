CREATE OR REPLACE PROCEDURE getAllRegisteredUsers()
BEGIN
	select * from users;
END;