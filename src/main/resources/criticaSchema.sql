-- Schema critica
DROP SCHEMA IF EXISTS critica CASCADE;
CREATE SCHEMA critica;

-- Users table
DROP TABLE IF EXISTS critica.users CASCADE;

CREATE TABLE critica.users (
                               idusers SERIAL PRIMARY KEY,
                               first_name VARCHAR(120) NOT NULL,
                               last_name VARCHAR(120),
                               username VARCHAR(60) NOT NULL,
                               email VARCHAR(60) NOT NULL,
                               password VARCHAR(255) NOT NULL,
                               createdat VARCHAR(120)
);

-- Reviews table
DROP TABLE IF EXISTS critica.reviews CASCADE;

CREATE TABLE critica.reviews (
                                 idreviews SERIAL PRIMARY KEY,
                                 user_id INT,
                                 song_id VARCHAR(120) NOT NULL,
                                 title VARCHAR(120),
                                 payload VARCHAR(280),
                                 rating INT DEFAULT 0,
                                 first_listen INT DEFAULT 0,
                                 created_at VARCHAR(60),
                                 CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES critica.users (idusers)
);
