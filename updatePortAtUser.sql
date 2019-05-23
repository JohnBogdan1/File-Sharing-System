CREATE OR REPLACE PROCEDURE updatePortAtUser(IN user_name varchar(25), IN portNumber INTEGER)
BEGIN
	update files set port=portNumber where userName=user_name;
END;