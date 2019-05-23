CREATE OR REPLACE PROCEDURE deleteUser(IN user_name varchar(25))
BEGIN
	delete from users where name = user_name;
	delete from group_users where userName = user_name;
	delete from groups where owner = user_name;
	delete from files where userName = user_name;
END;