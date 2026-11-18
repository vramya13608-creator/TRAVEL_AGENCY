-- Travel Agency Booking System: PostgreSQL schema (run in pgAdmin Query Tool)
-- Create the database first:  CREATE DATABASE travel_agency;

CREATE TABLE IF NOT EXISTS customer (
    customer_id   INTEGER PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    phone         CHAR(10)     NOT NULL CHECK (phone ~ '^[0-9]{10}$'),
    email         VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS destination (
    destination_id   SERIAL PRIMARY KEY,
    destination_name VARCHAR(80) NOT NULL UNIQUE,
    country          VARCHAR(60) DEFAULT 'India'
);

CREATE TABLE IF NOT EXISTS travel_package (
    package_id     INTEGER PRIMARY KEY,
    package_name   VARCHAR(100) NOT NULL,
    destination_id INTEGER NOT NULL REFERENCES destination(destination_id),
    duration_days  INTEGER NOT NULL CHECK (duration_days > 0),
    price          NUMERIC(10,2) NOT NULL CHECK (price > 0),
    max_capacity   INTEGER NOT NULL CHECK (max_capacity > 0)
);

CREATE TABLE IF NOT EXISTS booking (
    booking_id          INTEGER PRIMARY KEY,
    customer_id         INTEGER NOT NULL REFERENCES customer(customer_id),
    package_id          INTEGER NOT NULL REFERENCES travel_package(package_id),
    num_travelers       INTEGER NOT NULL CHECK (num_travelers > 0),
    booking_date        DATE NOT NULL,
    travel_date         DATE NOT NULL,
    status              VARCHAR(10) NOT NULL DEFAULT 'CONFIRMED' CHECK (status IN ('CONFIRMED','CANCELLED')),
    total_amount        NUMERIC(10,2) NOT NULL,
    cancellation_date   DATE,
    cancellation_reason VARCHAR(200),
    refund_amount       NUMERIC(10,2) DEFAULT 0,
    CHECK (travel_date > booking_date)
);

CREATE TABLE IF NOT EXISTS payment (
    payment_id     INTEGER PRIMARY KEY,
    booking_id     INTEGER NOT NULL REFERENCES booking(booking_id),
    amount         NUMERIC(10,2) NOT NULL,
    payment_method VARCHAR(15),
    payment_status VARCHAR(10) NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING','PAID','REFUNDED','CANCELLED')),
    payment_date   DATE,
    refund_amount  NUMERIC(10,2) DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_booking_package_date ON booking(package_id, travel_date);
CREATE INDEX IF NOT EXISTS idx_booking_customer     ON booking(customer_id);
