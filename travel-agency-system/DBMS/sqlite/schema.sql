-- Travel Agency Booking System: schema (SQLite; ANSI-style, easily ported to MySQL)
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS customer (
    customer_id   INTEGER PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    phone         CHAR(10)     NOT NULL CHECK (length(phone) = 10),
    email         VARCHAR(120) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS destination (
    destination_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    destination_name VARCHAR(80) NOT NULL UNIQUE,
    country          VARCHAR(60) DEFAULT 'India'
);

CREATE TABLE IF NOT EXISTS travel_package (
    package_id     INTEGER PRIMARY KEY,
    package_name   VARCHAR(100) NOT NULL,
    destination_id INTEGER NOT NULL,
    duration_days  INTEGER NOT NULL CHECK (duration_days > 0),
    price          DECIMAL(10,2) NOT NULL CHECK (price > 0),
    max_capacity   INTEGER NOT NULL CHECK (max_capacity > 0),
    FOREIGN KEY (destination_id) REFERENCES destination(destination_id)
);

CREATE TABLE IF NOT EXISTS booking (
    booking_id          INTEGER PRIMARY KEY,
    customer_id         INTEGER NOT NULL,
    package_id          INTEGER NOT NULL,
    num_travelers       INTEGER NOT NULL CHECK (num_travelers > 0),
    booking_date        DATE NOT NULL,
    travel_date         DATE NOT NULL,
    status              VARCHAR(10) NOT NULL DEFAULT 'CONFIRMED' CHECK (status IN ('CONFIRMED','CANCELLED')),
    total_amount        DECIMAL(10,2) NOT NULL,
    cancellation_date   DATE,
    cancellation_reason VARCHAR(200),
    refund_amount       DECIMAL(10,2) DEFAULT 0,
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id),
    FOREIGN KEY (package_id)  REFERENCES travel_package(package_id)
);

CREATE TABLE IF NOT EXISTS payment (
    payment_id     INTEGER PRIMARY KEY,
    booking_id     INTEGER NOT NULL,
    amount         DECIMAL(10,2) NOT NULL,
    payment_method VARCHAR(15),
    payment_status VARCHAR(10) NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING','PAID','REFUNDED','CANCELLED')),
    payment_date   DATE,
    refund_amount  DECIMAL(10,2) DEFAULT 0,
    FOREIGN KEY (booking_id) REFERENCES booking(booking_id)
);

CREATE INDEX IF NOT EXISTS idx_booking_package_date ON booking(package_id, travel_date);
CREATE INDEX IF NOT EXISTS idx_booking_customer     ON booking(customer_id);
