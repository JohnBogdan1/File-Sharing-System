CREATE OR REPLACE PROCEDURE deleteGroup(IN group_name varchar(25), IN group_owner varchar(25))
BEGIN
	DECLARE id INTEGER;

	if exists (select 1 from groups where groupName=group_name and owner=group_owner)
	then
		select groupID into id from groups where groupName=group_name and owner=group_owner;
		delete from groups where groupName=group_name and owner=group_owner;
		delete from group_users where groupID=id;
		update files set group_owned=NULL where userName=group_owner;
	end if;
END;