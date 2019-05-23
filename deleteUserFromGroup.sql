CREATE OR REPLACE PROCEDURE deleteUserFromGroup(IN group_name varchar(25), IN group_owner varchar(25), IN user varchar(25))
BEGIN
	DECLARE id INTEGER;

	if exists (select 1 from groups where groupName=group_name and owner=group_owner)
	then
		select groupID into id from groups where groupName=group_name and owner=group_owner;

		if exists (select 1 from group_users where groupID=id and userName=user) and user != group_owner
		then
			delete from group_users where groupID=id and userName=user;
		end if;

	end if;
END;