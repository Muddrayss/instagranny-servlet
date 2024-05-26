CREATE DATABASE IF NOT EXISTS db_instagranny;

USE db_instagranny;

CREATE TABLE IF NOT EXISTS GrannyUser (
	username VARCHAR(50) PRIMARY KEY,
	password VARCHAR(512)
);

CREATE TABLE IF NOT EXISTS Post (
	id INT PRIMARY KEY AUTO_INCREMENT,
	username VARCHAR(50),
	description TEXT,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY (username) REFERENCES GrannyUser(username)
);

CREATE TABLE IF NOT EXISTS Comment (
	id INT PRIMARY KEY AUTO_INCREMENT,
	post_id INT,
	username VARCHAR(50),
	comment TEXT,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	FOREIGN KEY (post_id) REFERENCES Post(id),
	FOREIGN KEY (username) REFERENCES GrannyUser(username)
);

CREATE TABLE IF NOT EXISTS PostLike (
	post_id INT,
	username VARCHAR(50),
	PRIMARY KEY (post_id, username),
	FOREIGN KEY (post_id) REFERENCES Post(id),
	FOREIGN KEY (username) REFERENCES GrannyUser(username)
);


-- Queries

-- Insert a new user
INSERT INTO user (username, password) VALUES ('user1', 'password1');

-- Insert a new post
INSERT INTO post (username, description) VALUES ('user1', 'This is a post');

-- Insert a new comment
INSERT INTO comment (post_id, username, comment) VALUES (1, 'user1', 'This is a comment');

-- Insert a new like
INSERT INTO like (post_id, username) VALUES (1, 'user1');

-- Select all posts
SELECT * FROM post;

-- Select comments for a post
SELECT * FROM comment WHERE post_id = 1;

-- Select likes for a post
SELECT * FROM like WHERE post_id = 1;