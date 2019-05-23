CREATE TABLE IF NOT EXISTS users (
	name varchar(25),
	password varchar(25)
);

CREATE TABLE IF NOT EXISTS groups (
	groupName varchar(25),
	owner varchar(25),
	groupID integer NOT NULL AUTO_INCREMENT,
	PRIMARY KEY (groupID)
);

CREATE TABLE IF NOT EXISTS group_users (
	groupID integer,
	userName varchar(25)
);

CREATE TABLE IF NOT EXISTS files (
	name varchar(25),
	ip varchar(24),
	port integer,
	userName varchar(25),
	group_owned varchar(25)
);

CREATE TABLE IF NOT EXISTS online_users (
	userName varchar(25),
	isOnline bit
);