CREATE DATABASE retro_smash;
USE retro_smash;
CREATE TABLE leaderboard (
    id INT AUTO_INCREMENT PRIMARY KEY,
    gamertag VARCHAR(50) NOT NULL,
    score INT
);
INSERT INTO leaderboard (gamertag, score)
VALUES ('PlayerOne', 120);
INSERT INTO leaderboard (gamertag, score)
VALUES ('GamerGirl', 200);
INSERT INTO leaderboard (gamertag, score)
VALUES ('ProGamer', 150);
INSERT INTO leaderboard (gamertag, score)
VALUES ('Speedy', 180);
INSERT INTO leaderboard (gamertag, score)
VALUES ('Ace', 170);
SELECT *
FROM leaderboard;