CREATE OR REPLACE PROCEDURE addMetaData(IN filename varchar(25), IN clientIp varchar(24), IN clientPort INTEGER, IN user_name varchar(25), IN group_name varchar(25))
BEGIN
	insert into files (name, ip, port, userName, group_owned) values(filename, clientIp, clientPort, user_name, group_name);
END;