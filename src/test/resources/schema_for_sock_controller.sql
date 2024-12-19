CREATE TABLE sock
(
    id          BIGSERIAL PRIMARY KEY,
    color       VARCHAR(32) NOT NULL,
    cotton_part INTEGER     NOT NULL,
    quantity    INTEGER     NOT NULL
);

INSERT INTO sock (color, cotton_part, quantity)
VALUES ('PURPLE', 30, 42),
       ('PURPLE', 45, 42),
       ('BLACK', 15, 108);

