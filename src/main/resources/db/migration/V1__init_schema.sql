CREATE TABLE users
(
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email        VARCHAR(255) NOT NULL UNIQUE,
    first_name   VARCHAR(255) NOT NULL,
    last_name    VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NOT NULL UNIQUE,
    balance      NUMERIC(19, 2) NOT NULL DEFAULT 0,
    login        VARCHAR(255) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,
    role         VARCHAR(50)  NOT NULL,
    status       VARCHAR(50)  NOT NULL
);

CREATE TABLE cars
(
    id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    brand               VARCHAR(255) NOT NULL,
    model               VARCHAR(255) NOT NULL,
    registration_number VARCHAR(255) NOT NULL UNIQUE,
    date_registration   DATE         NOT NULL,
    status              VARCHAR(50)  NOT NULL,
    condition           VARCHAR(50)  NOT NULL,
    price_per_hour      NUMERIC(19, 2),
    price_per_day       NUMERIC(19, 2)
);

CREATE TABLE bookings
(
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id    BIGINT NOT NULL,
    car_id     BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time   TIMESTAMP NOT NULL,
    status     VARCHAR(50) NOT NULL,
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_bookings_car FOREIGN KEY (car_id) REFERENCES cars (id)
);