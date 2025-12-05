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

DROP TABLE IF EXISTS public.follows CASCADE;

CREATE TABLE public.follows (
                                id SERIAL PRIMARY KEY,
                                follower_id INT NOT NULL,
                                following_id INT NOT NULL,
                                status VARCHAR(20),
                                created_at DATE,
                                CONSTRAINT fk_follower FOREIGN KEY (follower_id) REFERENCES public.users (idusers) ON DELETE CASCADE,
                                CONSTRAINT fk_following FOREIGN KEY (following_id) REFERENCES public.users (idusers) ON DELETE CASCADE,
                                CONSTRAINT unique_follow UNIQUE (follower_id, following_id)
);

CREATE TABLE IF NOT EXISTS public.conversations (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    user_one INT NOT NULL,
                                                    user_two INT NOT NULL,
                                                    user_min INT GENERATED ALWAYS AS (LEAST(user_one, user_two)) STORED,
                                                    user_max INT GENERATED ALWAYS AS (GREATEST(user_one, user_two)) STORED,
                                                    created_at TIMESTAMP,
                                                    last_updated TIMESTAMP,
                                                    CONSTRAINT fk_conv_user_one FOREIGN KEY (user_one) REFERENCES public.users (idusers),
                                                    CONSTRAINT fk_conv_user_two FOREIGN KEY (user_two) REFERENCES public.users (idusers),
                                                    CONSTRAINT ux_conversation_unique UNIQUE (user_min, user_max)
);


-- Messages
CREATE TABLE IF NOT EXISTS public.messages (
                                               id BIGSERIAL PRIMARY KEY,
                                               conversation_id BIGINT NOT NULL REFERENCES public.conversations(id) ON DELETE CASCADE,
                                               sender_id INT NOT NULL REFERENCES public.users (idusers),
                                               type VARCHAR(20) NOT NULL,
                                               content TEXT,
                                               metadata JSONB,
                                               created_at TIMESTAMP,
                                               seen BOOLEAN DEFAULT FALSE,
                                               deleted BOOLEAN DEFAULT FALSE
);


