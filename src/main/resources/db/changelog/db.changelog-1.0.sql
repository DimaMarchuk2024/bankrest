--liquibase formatted sql

--changeset KamoUser:1
CREATE TABLE users
(
    id              BIGSERIAL PRIMARY KEY,
    firstname       VARCHAR(64)        NOT NULL,
    lastname        VARCHAR(64)        NOT NULL,
    phone_number    VARCHAR(64) UNIQUE NOT NULL,
    email           VARCHAR(64) UNIQUE NOT NULL,
    role            VARCHAR(32)        NOT NULL,
    birth_date      DATE               NOT NULL,
    passport_number VARCHAR(64) UNIQUE NOT NULL,
    password        VARCHAR(128)       NOT NULL
);
--rollback DROP TABLE users;

--changeset KamoUser:2
CREATE TABLE card
(
    id              BIGSERIAL PRIMARY KEY,
    number          VARCHAR(64) UNIQUE                             NOT NULL,
    user_id         BIGINT REFERENCES users (id) ON DELETE CASCADE NOT NULL,
    expiration_date DATE                                           NOT NULL,
    status          VARCHAR(32)                                    NOT NULL,
    balance         NUMERIC(10, 2)                                 NOT NULL
);
--rollback DROP TABLE card;

--changeset KamoUser:3
CREATE TABLE transfer
(
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users (id) ON DELETE CASCADE NOT NULL,
    card_from       VARCHAR(64)                                    NOT NULL,
    card_to         VARCHAR(64)                                    NOT NULL,
    transfer_date   DATE                                           NOT NULL,
    sum             NUMERIC(10, 2)                                 NOT NULL
)
--rollback DROP TABLE transfer;