CREATE OR REPLACE PROCEDURE createGroup(IN group_name varchar(25), IN group_owner varchar(25))
BEGIN
	DECLARE id INTEGER;

	if not exists (select 1 from groups where groupName=group_name and owner=group_owner)
	then
		insert into groups (groupName, owner) values(group_name, group_owner);
		select groupID into id from groups where groupName=group_name and owner=group_owner;
		insert into group_users (groupID, userName) values(id, group_owner);
	end if;
END;