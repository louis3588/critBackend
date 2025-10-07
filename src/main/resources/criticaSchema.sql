-- Schema critica
DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

-- Users table
DROP TABLE IF EXISTS public.users CASCADE;

CREATE TABLE public.users (
                               idusers SERIAL PRIMARY KEY,
                               first_name VARCHAR(120) NOT NULL,
                               last_name VARCHAR(120),
                               username VARCHAR(60) NOT NULL,
                               email VARCHAR(60) NOT NULL,
                               password VARCHAR(255) NOT NULL,
                               createdat VARCHAR(120)
);

-- Reviews table
DROP TABLE IF EXISTS public.reviews CASCADE;

CREATE TABLE public.reviews (
                                 idreviews SERIAL PRIMARY KEY,
                                 user_id INT,
                                 song_id VARCHAR(120) NOT NULL,
                                 title VARCHAR(120),
                                 payload VARCHAR(280),
                                 rating INT DEFAULT 0,
                                 first_listen INT DEFAULT 0,
                                 created_at VARCHAR(60),
                                 CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES public.users (idusers)
);

DROP TABLE IF EXISTS public.user_details CASCADE;

CREATE TABLE public.user_details (
                              id SERIAL PRIMARY KEY,
                              user_id BIGINT UNIQUE REFERENCES public.users(idusers) ON DELETE CASCADE,
                              profile_picture VARCHAR(255),
                              date_of_birth DATE,
                              favourite_genre VARCHAR(100),
                              bio VARCHAR(280),
                              location VARCHAR(100)
);
