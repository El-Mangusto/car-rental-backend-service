CREATE SEQUENCE users_seq INCREMENT BY 50;

CREATE TABLE users
(
    id           BIGINT PRIMARY KEY DEFAULT nextval('users_seq'),
    email        VARCHAR(255)   NOT NULL UNIQUE,
    first_name   VARCHAR(255)   NOT NULL,
    last_name    VARCHAR(255)   NOT NULL,
    phone_number VARCHAR(255)   NOT NULL UNIQUE,
    balance      NUMERIC(19, 2) NOT NULL DEFAULT 0,
    login        VARCHAR(255)   NOT NULL UNIQUE,
    password     VARCHAR(255)   NOT NULL,
    role         VARCHAR(50)    NOT NULL,
    status       VARCHAR(50)    NOT NULL
);

ALTER SEQUENCE users_seq OWNED BY users.id;



CREATE SEQUENCE cars_seq INCREMENT BY 50;

CREATE TABLE cars
(
    id                  BIGINT PRIMARY KEY DEFAULT nextval('cars_seq'),
    brand               VARCHAR(255)  NOT NULL,
    model               VARCHAR(255)  NOT NULL,
    registration_number VARCHAR(255)  NOT NULL UNIQUE,
    date_registration   DATE          NOT NULL,
    status              VARCHAR(50)   NOT NULL,
    condition           VARCHAR(50)   NOT NULL,
    price_per_hour      NUMERIC(19, 2),
    price_per_day       NUMERIC(19, 2)
);

ALTER SEQUENCE cars_seq OWNED BY cars.id;



CREATE SEQUENCE bookings_seq INCREMENT BY 50;

CREATE TABLE bookings
(
    id         BIGINT PRIMARY KEY DEFAULT nextval('bookings_seq'),
    user_id    BIGINT       NOT NULL,
    car_id     BIGINT       NOT NULL,
    start_time TIMESTAMP    NOT NULL,
    end_time   TIMESTAMP    NOT NULL,
    status     VARCHAR(50)  NOT NULL,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_bookings_car FOREIGN KEY (car_id) REFERENCES cars (id)
);

ALTER SEQUENCE bookings_seq OWNED BY bookings.id;