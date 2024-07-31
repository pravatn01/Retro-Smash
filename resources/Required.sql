CREATE DATABASE retro_smash;

USE retro_smash;

CREATE TABLE leaderboard (
    id INT AUTO_INCREMENT PRIMARY KEY,
    gamertag VARCHAR(50) NOT NULL,
    score INT
);

INSERT INTO leaderboard (gamertag, score)
VALUES ('PlayerOne', 50);
INSERT INTO leaderboard (gamertag, score)
VALUES ('GamerGirl', 60);
INSERT INTO leaderboard (gamertag, score)
VALUES ('ProGamer', 80);
INSERT INTO leaderboard (gamertag, score)
VALUES ('Speedy', 10);
INSERT INTO leaderboard (gamertag, score)
VALUES ('Ace', 120);

SELECT * FROM leaderboard;

TRUNCATE TABLE leaderboard;