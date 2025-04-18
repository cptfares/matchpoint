CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('admin', 'referee'))
);

CREATE TABLE matches (
    id SERIAL PRIMARY KEY,
    player1 VARCHAR(50) NOT NULL,
    player2 VARCHAR(50) NOT NULL,
    match_date DATE NOT NULL,
    time TIME NOT NULL,
    location VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'scheduled'
);

CREATE TABLE scores (
    id SERIAL PRIMARY KEY,
    match_id INT NOT NULL REFERENCES matches(id) ON DELETE CASCADE,
    player1_score INT NOT NULL DEFAULT 0,
    player2_score INT NOT NULL DEFAULT 0
);

